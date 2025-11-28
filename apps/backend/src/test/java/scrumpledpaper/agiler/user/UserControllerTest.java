package scrumpledpaper.agiler.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import scrumpledpaper.agiler.annotation.IntegrationTest;
import scrumpledpaper.agiler.common.AuthContext;
import scrumpledpaper.agiler.common.TestDataFactory;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.fixture.UserFixture;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.image.repository.ImageRepository;
import scrumpledpaper.agiler.user.dto.UserResDto;
import scrumpledpaper.agiler.user.dto.UserUpdateImageReqDto;
import scrumpledpaper.agiler.user.dto.UserUpdateReqDto;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@Transactional
public class UserControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ImageRepository imageRepository;
	@Autowired
	private TestDataFactory testDataFactory;
	Image defaultImage;

	@Nested
	@DisplayName("Get User Test")
	class GetUserTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - User Get Success")
		public void userGetSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			// when
			String res = mockMvc.perform(
							get("/api/v1/users")
									.cookie(new Cookie("accessToken", auth.getToken()))
									.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString();
			// then
			UserResDto userResDto = objectMapper.readValue(res, UserResDto.class);
			assertThat(userResDto.nickname()).isEqualTo(auth.getUser().getNickname());
			Image image = imageRepository.findById(auth.getUser().getImageId())
					.orElseThrow();
			assertThat(userResDto.imageUrl()).isEqualTo(image.getUrl());
		}

		@Test
		@DisplayName("404 - Image Not Found")
		public void notFoundImage() throws Exception {
			// given
			User user = testDataFactory.createUser(9999L);
			String accessToken = testDataFactory.createAccessToken(user);
			// when
			String res = mockMvc.perform(
							get("/api/v1/users")
									.cookie(new Cookie("accessToken", accessToken))
									.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isNotFound())
					.andReturn().getResponse().getContentAsString();
			// then
			assertThat(res).contains(ErrorCode.IMAGE_NOT_FOUND.getCode());
		}
	}

	@Nested
	@DisplayName("Update User Test")
	class UpdateUserTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - User Update Success")
		public void userUpdateSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String updateNickname = "newNickname";
			UserUpdateReqDto updateReqDto = UserFixture.createUpdateReqDto(updateNickname);
			String updateJson = objectMapper.writeValueAsString(updateReqDto);
			// when
			mockMvc.perform(
							patch("/api/v1/users")
									.cookie(new Cookie("accessToken", auth.getToken()))
									.contentType(MediaType.APPLICATION_JSON)
									.content(updateJson))
					.andExpect(status().isNoContent());
			// then
			User updatedUser = userRepository.findById(auth.getUser().getId())
					.orElseThrow();
			assertThat(updatedUser.getNickname()).isEqualTo(updateNickname);
		}

	}

	@Nested
	@DisplayName("Update User Image Test")
	class UpdateUserImageTest {

		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - Default Image to New Image Success")
		public void userImageUpdateSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String newImageObjectKey = "new-object-key";
			UserUpdateImageReqDto request = new UserUpdateImageReqDto(newImageObjectKey);
			String updateJson = objectMapper.writeValueAsString(request);

			// when
			mockMvc.perform(
							patch("/api/v1/users/image")
									.cookie(new Cookie("accessToken", auth.getToken()))
									.contentType(MediaType.APPLICATION_JSON)
									.content(updateJson))
					.andExpect(status().isNoContent());

			// then
			Image newImage = testDataFactory.findImageById(auth.getUser().getImageId());
			assertThat(newImage.getObjectKey()).isEqualTo(newImageObjectKey);
		}

		@Test
		@DisplayName("204 - Existing Image to New Image Success")
		public void userImageUpdateFromExistingImageSuccess() throws Exception {
			// given
			Image defaultImage = testDataFactory.createDefaultImage();
			AuthContext auth = testDataFactory.createAuth(defaultImage);

			Image existingImage = testDataFactory.createImage("existing-object-key", "http://example.com/existing-image");
			testDataFactory.setUserImage(auth.getUser(), existingImage);

			String newImageObjectKey = "new-object-key";
			UserUpdateImageReqDto request = new UserUpdateImageReqDto(newImageObjectKey);
			String updateJson = objectMapper.writeValueAsString(request);

			// when
			mockMvc.perform(
							patch("/api/v1/users/image")
									.cookie(new Cookie("accessToken", auth.getToken()))
									.contentType(MediaType.APPLICATION_JSON)
									.content(updateJson))
					.andExpect(status().isNoContent());

			// then
			Image newImage = testDataFactory.findImageById(auth.getUser().getImageId());
			assertThat(auth.getUser().getImageId()).isEqualTo(newImage.getId());
			assertThat(newImage.getObjectKey()).isEqualTo(newImageObjectKey);
		}

		@Test
		@DisplayName("400 - Non ObjectKey User Image Update")
		public void nonObjectKeyUserImageUpdate() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			UserUpdateImageReqDto request = new UserUpdateImageReqDto("");
			String updateJson = objectMapper.writeValueAsString(request);

			// when & then
			mockMvc.perform(
							patch("/api/v1/users/image")
									.cookie(new Cookie("accessToken", auth.getToken()))
									.contentType(MediaType.APPLICATION_JSON)
									.content(updateJson))
					.andExpect(status().isBadRequest());
		}

	}

	@Nested
	@DisplayName("Delete User Image Test")
	class DeleteUserImageTest {

		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - User Image Delete When Default Image Success")
		public void userImageDeleteWhenDefaultImageSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);

			// when
			mockMvc.perform(
							delete("/api/v1/users/image")
									.cookie(new Cookie("accessToken", auth.getToken())))
					.andExpect(status().isNoContent());

			// then
			assertThat(auth.getUser().getImageId()).isEqualTo(TestDataFactory.DEFAULT_IMAGE_ID);
		}

		@Test
		@DisplayName("204 - User Image Delete Success")
		public void userImageDeleteSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			Image existingImage = testDataFactory.createImage("existing-object-key", "http://example.com/existing-image");
			testDataFactory.setUserImage(auth.getUser(), existingImage);

			// when
			mockMvc.perform(
							delete("/api/v1/users/image")
									.cookie(new Cookie("accessToken", auth.getToken())))
					.andExpect(status().isNoContent());

			// then
			assertThat(auth.getUser().getImageId()).isEqualTo(TestDataFactory.DEFAULT_IMAGE_ID);
		}

	}

}
