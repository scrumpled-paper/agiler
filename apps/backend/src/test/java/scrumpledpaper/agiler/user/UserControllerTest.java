package scrumpledpaper.agiler.user;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import scrumpledpaper.agiler.annotation.IntegrationTest;
import scrumpledpaper.agiler.fixture.ImageFixture;
import scrumpledpaper.agiler.fixture.TokenFixture;
import scrumpledpaper.agiler.fixture.UserFixture;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.image.repository.ImageRepository;
import scrumpledpaper.agiler.user.dto.UserResDto;
import scrumpledpaper.agiler.user.dto.UserUpdateReqDto;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.repository.UserRepository;

@IntegrationTest
public class UserControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private TokenFixture tokenFixture;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ImageRepository imageRepository;
	Image defaultImage;

	@Nested
	@DisplayName("Get User Test")
	class GetUserTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = ImageFixture.createImage();
			imageRepository.save(defaultImage);
		}

		@Test
		@DisplayName("200 - User Get Success")
		public void userGetSuccess() throws Exception {
			// given
			User user = UserFixture.createUser(defaultImage);
			userRepository.save(user);
			String accessToken = tokenFixture.createAccessToken(user);
			// when
			String res = mockMvc.perform(
					get("/api/v1/users")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
			// then
			UserResDto userResDto = objectMapper.readValue(res, UserResDto.class);
			assertThat(userResDto.nickname()).isEqualTo(user.getNickname());
			Image image = imageRepository.findById(user.getImageId())
				.orElseThrow();
			assertThat(userResDto.imageUrl()).isEqualTo(image.getUrl());
		}

		@Test
		@DisplayName("404 - Image Not Found")
		public void notFoundImage() throws Exception {
			// given
			User user = UserFixture.createUser(999L);
			userRepository.save(user);
			String accessToken = tokenFixture.createAccessToken(user);
			// when
			String res = mockMvc.perform(
					get("/api/v1/users")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();
			// then
			assertThat(res).contains("I001").contains("이미지를 찾을 수 없습니다");
		}
	}

	@Nested
	@DisplayName("Update User Test")
	class UpdateUserTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = ImageFixture.createImage();
			imageRepository.save(defaultImage);
		}

		@Test
		@DisplayName("204 - User Update Success")
		public void userUpdateSuccess() throws Exception {
			// given
			User user = UserFixture.createUser(defaultImage);
			userRepository.save(user);
			String accessToken = tokenFixture.createAccessToken(user);
			UserUpdateReqDto updateReqDto = UserFixture.createUpdateReqDto("newNickname");
			String updateJson = objectMapper.writeValueAsString(updateReqDto);
			// when
			mockMvc.perform(
					patch("/api/v1/users")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNoContent());
			// then
			User updatedUser = userRepository.findById(user.getId())
				.orElseThrow();
			assertThat(updatedUser.getNickname()).isEqualTo("newNickname");
		}

		@Test
		@DisplayName("404 - User Not Found")
		public void notFoundUser() throws Exception {
			// given
			String accessToken = tokenFixture.createNotAllowedAccessToken();
			UserUpdateReqDto updateReqDto = UserFixture.createUpdateReqDto("newNickname");
			String updateJson = objectMapper.writeValueAsString(updateReqDto);
			// when
			String res = mockMvc.perform(
					patch("/api/v1/users")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();
			// then
			assertThat(res).contains("U001").contains("사용자를 찾을 수 없습니다");
		}
	}
}
