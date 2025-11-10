package scrumpledpaper.agiler.user;

import com.fasterxml.jackson.core.type.TypeReference;
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
import scrumpledpaper.agiler.common.PageResDto;
import scrumpledpaper.agiler.common.TestDataFactory;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.project.dto.ProfileResDto;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.entity.Role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
	@DisplayName("Get Project Member List Test")
	class GetProjectMemberTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 프로젝트 멤버 조회 성공")
		void getProjectProfilesSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			AuthContext member1 = testDataFactory.createAuth(testDataFactory.createDefaultImage());
			AuthContext member2 = testDataFactory.createAuth(testDataFactory.createDefaultImage());
			testDataFactory.createProfile(member1.getUser(), project, Role.MEMBER);
			testDataFactory.createProfile(member2.getUser(), project, Role.MEMBER);

			// when
			String response = mockMvc.perform(
							get("/api/v1/projects/{projectUrl}/profiles", url)
									.cookie(new Cookie("accessToken", auth.getToken()))
									.param("page", "0")
									.param("size", "10"))
					.andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<ProfileResDto> page = objectMapper.readValue(response,
					new TypeReference<PageResDto<ProfileResDto>>() {
					});

			assertThat(page.getTotalElements()).isEqualTo(3);
			assertThat(page.getContents()).hasSize(3);
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트 조회 실패")
		void getProjectProfilesNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String nonExistingUrl = "non-existing_url";

			// when
			String response = mockMvc.perform(
							get("/api/v1/projects/{projectUrl}/profiles", nonExistingUrl)
									.cookie(new Cookie("accessToken", auth.getToken()))
									.param("page", "0")
									.param("size", "10"))
					.andExpect(status().isNotFound())
					.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getCode());
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 유저의 조회 실패")
		void getProjectProfilesForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext anotherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());

			// when
			String response = mockMvc.perform(
							get("/api/v1/projects/{projectUrl}/profiles", url)
									.cookie(new Cookie("accessToken", anotherAuth.getToken()))
									.param("page", "0")
									.param("size", "10"))
					.andExpect(status().isForbidden())
					.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getCode());
		}

		@Test
		@DisplayName("400 - 잘못된 페이지 요청")
		void getProjectProfilesInvalidPage() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());

			// when
			String response = mockMvc.perform(
							get("/api/v1/projects/{projectUrl}/profiles", url)
									.cookie(new Cookie("accessToken", auth.getToken()))
									.param("page", "99")
									.param("size", "10"))
					.andExpect(status().isNotFound())
					.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PAGE_NOT_FOUND.getCode());
		}
	}

	@Nested
	@DisplayName("Get My project Profile Test")
	class GetMyProjectProfileTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 내 프로젝트 프로필 조회 성공")
		public void getMyProjectProfileOwnerSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProject(url);
			Profile profile = testDataFactory.createProfile(auth.getUser(), project, Role.OWNER);

			// when
			String response = mockMvc.perform(
							get("/api/v1/projects/{projectUrl}/profiles/me", url)
									.cookie(new Cookie("accessToken", auth.getToken()))
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
		public void getMyProjectProfileMemberSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext memberAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile profile = testDataFactory.createProfile(memberAuth.getUser(), project, Role.MEMBER);

			// when
			String response = mockMvc.perform(
							get("/api/v1/projects/{projectUrl}/profiles/me", url)
									.cookie(new Cookie("accessToken", memberAuth.getToken()))
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
		public void getMyProjectProfileNotFoundProject() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String invalidProjectUrl = "invalid_url";

			// when
			String response = mockMvc.perform(
							get("/api/v1/projects/{projectUrl}/profiles/me", invalidProjectUrl)
									.cookie(new Cookie("accessToken", auth.getToken()))
									.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isNotFound())
					.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자가 프로필 조회 시도")
		public void getMyProjectProfileNotMemberForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext nonMemberAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());

			// when
			String response = mockMvc.perform(
							get("/api/v1/projects/{projectUrl}/profiles/me", url)
									.cookie(new Cookie("accessToken", nonMemberAuth.getToken()))
									.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isForbidden())
					.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}
	}

	@Nested
	@DisplayName("Get Project Profile By Id Test")
	class GetProjectProfileByIdTest {

		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 프로젝트 멤버 프로필 조회 성공 (오너가 멤버 조회)")
		public void getProjectProfileByIdOwnerSuccess() throws Exception {
			// given
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			AuthContext memberAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());
			Profile memberProfile = testDataFactory.createProfile(memberAuth.getUser(), project, Role.MEMBER);

			// when
			String response = mockMvc.perform(
							get("/api/v1/projects/{projectUrl}/profiles/{profileId}", url, memberProfile.getId())
									.cookie(new Cookie("accessToken", ownerAuth.getToken()))
									.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString();

			// then
			ProfileResDto profileResDto = objectMapper.readValue(response, ProfileResDto.class);
			assertThat(profileResDto.memberId()).isEqualTo(memberProfile.getId());
			assertThat(profileResDto.nickname()).isEqualTo(memberProfile.getNickname());
			assertThat(profileResDto.imageUrl()).isEqualTo(defaultImage.getUrl());
			assertThat(profileResDto.role()).isEqualTo(Role.MEMBER.name());
			assertThat(profileResDto.description()).isEqualTo(memberProfile.getDescription());
			assertThat(profileResDto.email()).isEqualTo(memberAuth.getUser().getEmail());
		}

		@Test
		@DisplayName("200 - 프로젝트 오너 프로필 조회 성공 (멤버가 오너 조회)")
		public void getProjectProfileByIdMemberSuccess() throws Exception {
			// given
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			AuthContext memberAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());
			Profile ownerProfile = testDataFactory.findProfileByUserIdAndProjectId(ownerAuth.getUser().getId(), project.getId());
			testDataFactory.createProfile(memberAuth.getUser(), project, Role.MEMBER);

			// when
			String response = mockMvc.perform(
							get("/api/v1/projects/{projectUrl}/profiles/{profileId}", url, ownerProfile.getId())
									.cookie(new Cookie("accessToken", memberAuth.getToken()))
									.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString();

			// then
			ProfileResDto profileResDto = objectMapper.readValue(response, ProfileResDto.class);
			assertThat(profileResDto.memberId()).isEqualTo(ownerProfile.getId());
			assertThat(profileResDto.nickname()).isEqualTo(ownerProfile.getNickname());
			assertThat(profileResDto.role()).isEqualTo(Role.OWNER.name());
			assertThat(profileResDto.email()).isEqualTo(ownerAuth.getUser().getEmail());
		}

		@Test
		@DisplayName("200 - 자신의 프로필 조회")
		public void getMyProfileById() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile myProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());

			// when
			String response = mockMvc.perform(
							get("/api/v1/projects/{projectUrl}/profiles/{profileId}", url, myProfile.getId())
									.cookie(new Cookie("accessToken", auth.getToken()))
									.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString();

			// then
			ProfileResDto profileResDto = objectMapper.readValue(response, ProfileResDto.class);
			assertThat(profileResDto.memberId()).isEqualTo(myProfile.getId());
			assertThat(profileResDto.email()).isEqualTo(auth.getUser().getEmail());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로필 ID")
		public void getProjectProfileByIdNotFoundProfile() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Long nonExistentProfileId = 99999L;

			// when
			String response = mockMvc.perform(
							get("/api/v1/projects/{projectUrl}/profiles/{profileId}", url, nonExistentProfileId)
									.cookie(new Cookie("accessToken", auth.getToken()))
									.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isNotFound())
					.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_PROFILE_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트")
		public void getProjectProfileByIdNotFoundProject() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String invalidUrl = "invalid_url";
			Long profileId = 9999L;

			// when
			String response = mockMvc.perform(
							get("/api/v1/projects/{projectUrl}/profiles/{profileId}", invalidUrl, profileId)
									.cookie(new Cookie("accessToken", auth.getToken()))
									.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isNotFound())
					.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자가 프로필 조회 시도")
		public void getProjectProfileByIdNotMemberForbidden() throws Exception {
			// given
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			AuthContext nonMemberAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());
			Profile ownerProfile = testDataFactory.findProfileByUserIdAndProjectId(ownerAuth.getUser().getId(), project.getId());

			// when
			String response = mockMvc.perform(
							get("/api/v1/projects/{projectUrl}/profiles/{profileId}", url, ownerProfile.getId())
									.cookie(new Cookie("accessToken", nonMemberAuth.getToken()))
									.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isForbidden())
					.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("403 - 다른 프로젝트의 프로필 조회 시도")
		public void getProjectProfileByIdDifferentProjectForbidden() throws Exception {
			// given
			AuthContext auth1 = testDataFactory.createAuth(defaultImage);
			AuthContext auth2 = testDataFactory.createAuth(defaultImage);
			String url1 = "project1_url";
			String url2 = "project2_url";
			Project project1 = testDataFactory.createProjectAndOwnerProfile(url1, auth1.getUser());
			Project project2 = testDataFactory.createProjectAndOwnerProfile(url2, auth2.getUser());
			Profile profile2 = testDataFactory.findProfileByUserIdAndProjectId(auth2.getUser().getId(), project2.getId());

			// when
			String response = mockMvc.perform(
							get("/api/v1/projects/{projectUrl}/profiles/{profileId}", url1, profile2.getId())
									.cookie(new Cookie("accessToken", auth1.getToken()))
									.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isNotFound())
					.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_PROFILE_NOT_FOUND.getMessage());
		}
	}
}





