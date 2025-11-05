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
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import scrumpledpaper.agiler.annotation.IntegrationTest;
import scrumpledpaper.agiler.common.AuthContext;
import scrumpledpaper.agiler.common.TestDataFactory;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.project.dto.ProfileResDto;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.user.entity.Profile;
import scrumpledpaper.agiler.user.entity.Role;

@IntegrationTest
@Transactional
public class ProfileControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private TestDataFactory testDataFactory;
	Image defaultImage;

	@Nested
	@DisplayName("프로필 조회")
	class GetProfile {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 프로젝트 프로필 조회 성공")
		public void getProjectProfileOwnerSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProject(url);
			Profile profile = testDataFactory.createProfile(auth.getUser(), project, Role.OWNER);

			// when
			String response = mockMvc.perform(
					get("/api/v1/profiles/{projectUrl}", url)
						.header("Authorization", auth.bearer())
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			ProfileResDto profileResDto = objectMapper.readValue(response, ProfileResDto.class);
			assertThat(profileResDto.memberId()).isEqualTo(profile.getId());
			assertThat(profileResDto.nickname()).isEqualTo(profile.getNickname());
			assertThat(profileResDto.imageUrl()).isEqualTo(defaultImage.getUrl());
			assertThat(profileResDto.role()).isEqualTo(profile.getRole().name());
			assertThat(profileResDto.description()).isEqualTo(profile.getDescription());
			assertThat(profileResDto.email()).isEqualTo(auth.getUser().getEmail());
		}

		@Test
		@DisplayName("200 - 프로젝트 프로필 조회 성공 - member")
		public void getProjectProfileMemberSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext memberAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile profile = testDataFactory.createProfile(memberAuth.getUser(), project, Role.MEMBER);

			// when
			String response = mockMvc.perform(
					get("/api/v1/profiles/{projectUrl}", url)
						.header("Authorization", memberAuth.bearer())
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			ProfileResDto profileResDto = objectMapper.readValue(response, ProfileResDto.class);
			assertThat(profileResDto.memberId()).isEqualTo(profile.getId());
			assertThat(profileResDto.nickname()).isEqualTo(profile.getNickname());
			assertThat(profileResDto.imageUrl()).isEqualTo(defaultImage.getUrl());
			assertThat(profileResDto.role()).isEqualTo(profile.getRole().name());
			assertThat(profileResDto.description()).isEqualTo(profile.getDescription());
			assertThat(profileResDto.email()).isEqualTo(auth.getUser().getEmail());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트")
		public void getProjectProfileNotFoundProject() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String invalidProjectUrl = "invalid_url";

			// when
			String response = mockMvc.perform(
					get("/api/v1/profiles/{projectUrl}", invalidProjectUrl)
						.header("Authorization", auth.bearer())
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자가 프로필 조회 시도")
		public void getProjectProfileNotMemberForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext nonMemberAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());

			// when
			String response = mockMvc.perform(
					get("/api/v1/profiles/{projectUrl}", url)
						.header("Authorization", nonMemberAuth.bearer())
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}
	}
}





