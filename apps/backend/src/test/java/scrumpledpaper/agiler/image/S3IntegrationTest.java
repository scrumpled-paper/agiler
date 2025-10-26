package scrumpledpaper.agiler.image;

import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import scrumpledpaper.agiler.annotation.IntegrationTest;
import scrumpledpaper.agiler.fixture.ImageFixture;
import scrumpledpaper.agiler.fixture.TokenFixture;
import scrumpledpaper.agiler.fixture.UserFixture;
import scrumpledpaper.agiler.image.dto.ImageUploadConfirmationRequestDto;
import scrumpledpaper.agiler.image.dto.ImageUploadConfirmationResponseDto;
import scrumpledpaper.agiler.image.dto.PreSignedUrlRequestDto;
import scrumpledpaper.agiler.image.dto.PreSignedUrlResponseDto;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.image.repository.ImageRepository;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class S3IntegrationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper om;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private TokenFixture tokenFixture;
	@Autowired
	private ImageRepository imageRepository;
	private String accessToken;
	@Autowired
	private AmazonS3 amazonS3Client;
	@Value("${cloud.aws.s3.bucket}")
	private String bucket;
	private User user;

	@BeforeEach
	void setUp() {
		Image image = ImageFixture.createImage();
		imageRepository.save(image);
		User user = UserFixture.createUser(image);
		this.user = user;
		userRepository.save(user);
		accessToken = tokenFixture.createAccessToken(user);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"image/png",
			"image/jpg",
			"image/jpeg",
			"image/gif"
	})
	@DisplayName("200 - Post Pre-signed URL")
	void getPresignedUrl(String contentType) throws Exception {
		// given
		PreSignedUrlRequestDto req = new PreSignedUrlRequestDto("example-image.png", contentType);

		// when
		String res = mockMvc.perform(
						post("/api/v1/s3/pre-signed-url")
								.contentType(MediaType.APPLICATION_JSON)
								.header("Authorization", "Bearer " + accessToken)
								.content(om.writeValueAsString(req)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		PreSignedUrlResponseDto resDto = om.readValue(res, PreSignedUrlResponseDto.class);

		// then
		assertThat(resDto.objectKey()).isNotBlank();
		assertThat(resDto.objectKey()).contains("example-image.png");
		assertThat(resDto.preSignedUrl()).isNotBlank();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"",
			"   ",
			"invalid-content-type",
			"application/pdf",
			"text/plain",
			"shell/script"
	})
	@DisplayName("400 - Post Pre-signed URL Fail - Unsupported Content Type")
	void getPresignedUrlFailUnsupportedContentType(String contentType) throws Exception {
		// given
		PreSignedUrlRequestDto req = new PreSignedUrlRequestDto("example-image.png", contentType);

		// when & then
		mockMvc.perform(
						post("/api/v1/s3/pre-signed-url")
								.contentType(MediaType.APPLICATION_JSON)
								.header("Authorization", "Bearer " + accessToken)
								.content(om.writeValueAsString(req)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("200 - Post Confirm Upload Success")
	void confirmUploadSuccess() throws Exception {
		// given
		String objectKey = "images/" + user.getId() + "/test-image.png";
		String filename = "test-image.png";
		amazonS3Client.putObject(bucket, objectKey, filename);

		ImageUploadConfirmationRequestDto req = new ImageUploadConfirmationRequestDto(objectKey);

		// when
		String res = mockMvc.perform(
						post("/api/v1/s3/confirm-upload")
								.contentType(MediaType.APPLICATION_JSON)
								.header("Authorization", "Bearer " + accessToken)
								.content(om.writeValueAsString(req)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		ImageUploadConfirmationResponseDto resDto = om.readValue(res, ImageUploadConfirmationResponseDto.class);

		// then
		String expectedUrl = amazonS3Client.getUrl(bucket, objectKey).toString();
		Image savedImage = imageRepository.findById(resDto.imageId()).orElseThrow();

		assertThat(savedImage.getId()).isEqualTo(resDto.imageId());
		assertThat(savedImage.getObjectKey()).isEqualTo(objectKey);
		assertThat(savedImage.getUrl()).isEqualTo(expectedUrl);
		assertThat(resDto.imageUrl()).isEqualTo(expectedUrl);
	}

	@Test
	@DisplayName("204 - Delete Image Success")
	void deleteImageSuccess() throws Exception {
		// given
		Image image = ImageFixture.createImage();
		imageRepository.save(image);

		amazonS3Client.putObject(bucket, image.getObjectKey(), "image.jpg");

		// when
		mockMvc.perform(delete("/api/v1/s3/{imageId}", image.getId())
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isNoContent());

		// then
		assertThatThrownBy(() -> imageRepository.findById(image.getId()).orElseThrow())
				.isInstanceOf(Exception.class);

		boolean existsInS3 = amazonS3Client.doesObjectExist(bucket, image.getObjectKey());
		assertThat(existsInS3).isFalse();
	}

	@Test
	@DisplayName("404 - Delete Image Fail - Image Not Found")
	void deleteImageFailImageNotFound() throws Exception {
		// given
		Long nonExistentImageId = 9999L;

		// when & then
		mockMvc.perform(delete("/api/v1/s3/{imageId}", nonExistentImageId)
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isNotFound());
	}

}
