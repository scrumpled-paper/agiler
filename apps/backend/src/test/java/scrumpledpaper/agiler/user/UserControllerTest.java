package scrumpledpaper.agiler.user;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import scrumpledpaper.agiler.fixture.ImageFixture;
import scrumpledpaper.agiler.fixture.TokenFixture;
import scrumpledpaper.agiler.fixture.UserFixture;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.image.repository.ImageRepository;
import scrumpledpaper.agiler.user.dto.UserResDto;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
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

	@Nested
	@DisplayName("Get User Test")
	class GetUserTest {
		@BeforeEach
		void beforeEach() {
			Image image = ImageFixture.createImage();
			imageRepository.save(image);
		}

		@Test
		@DisplayName("200 - User Get Success")
		public void userGetSuccess() throws Exception {
			// given
			User user = UserFixture.createUser();
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
			assertThat(userResDto.getNickname()).isEqualTo(user.getNickname());
			Image image = imageRepository.findById(user.getImageId())
				.orElseThrow();
			assertThat(userResDto.getImageUrl()).isEqualTo(image.getUrl());
		}

		@Test
		@DisplayName("404 - Not Found Image")
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
}
