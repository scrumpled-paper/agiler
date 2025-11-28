package scrumpledpaper.agiler.image;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import scrumpledpaper.agiler.annotation.IntegrationTest;
import scrumpledpaper.agiler.common.TestDataFactory;
import scrumpledpaper.agiler.image.dto.PreSignedUrlRequestDto;
import scrumpledpaper.agiler.image.dto.PreSignedUrlResponseDto;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.user.entity.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class S3IntegrationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper om;
	@Autowired
	private TestDataFactory testDataFactory;
	private String cookie;

	@BeforeEach
	void setUp() {
		Image image = testDataFactory.createDefaultImage();
		User user = testDataFactory.createUser(image.getId());
		cookie = testDataFactory.createAccessToken(user);
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
								.cookie(new Cookie("accessToken", cookie))
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
								.cookie(new Cookie("accessToken", cookie))
								.content(om.writeValueAsString(req)))
				.andExpect(status().isBadRequest());
	}

}
