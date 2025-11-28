package scrumpledpaper.agiler.project;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import scrumpledpaper.agiler.project.dto.ImageUpdateReqDto;
import scrumpledpaper.agiler.project.dto.ProfileResDto;
import scrumpledpaper.agiler.project.dto.ProfileRoleUpdateReqDto;
import scrumpledpaper.agiler.project.dto.ProfileUpdateReqDto;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.entity.Role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
			String url = "test-url";
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
			String url = "test-url";
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
			String url = "test-url";
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
			String url = "test-url";
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
			assertThat(profileResDto.profileId()).isEqualTo(profile.getId());
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
			String url = "test-url";
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
			assertThat(profileResDto.profileId()).isEqualTo(profile.getId());
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
			String url = "test-url";
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
			String url = "test-url";
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
			assertThat(profileResDto.profileId()).isEqualTo(memberProfile.getId());
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
			String url = "test-url";
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
			assertThat(profileResDto.profileId()).isEqualTo(ownerProfile.getId());
			assertThat(profileResDto.nickname()).isEqualTo(ownerProfile.getNickname());
			assertThat(profileResDto.role()).isEqualTo(Role.OWNER.name());
			assertThat(profileResDto.email()).isEqualTo(ownerAuth.getUser().getEmail());
		}

		@Test
		@DisplayName("200 - 자신의 프로필 조회")
		public void getMyProfileById() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
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
			assertThat(profileResDto.profileId()).isEqualTo(myProfile.getId());
			assertThat(profileResDto.email()).isEqualTo(auth.getUser().getEmail());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로필 ID")
		public void getProjectProfileByIdNotFoundProfile() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
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
			String url = "test-url";
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

	@Nested
	@DisplayName("Update Project Profile Test")
	class UpdateProjectProfileTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - 프로젝트 프로필 수정 성공")
		public void updateProjectProfileSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			ProfileUpdateReqDto updateReqDto = new ProfileUpdateReqDto(
				"NewNickname",
				"newEmail@example.com",
				"New description");
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/profiles", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getContentAsString();

			// then
			Profile updatedProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
			assertThat(updatedProfile.getNickname()).isEqualTo(updateReqDto.nickname());
			assertThat(updatedProfile.getEmail()).isEqualTo(updateReqDto.email());
			assertThat(updatedProfile.getDescription()).isEqualTo(updateReqDto.description());
		}

		@Test
		@DisplayName("204 - 기존 값과 같은 값일때 프로필 수정 성공")
		public void updateProjectProfileWithSameValuesSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile profile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
			ProfileUpdateReqDto updateReqDto = new ProfileUpdateReqDto(
				profile.getNickname(),
				profile.getEmail(),
				profile.getDescription()
			);
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/profiles", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getContentAsString();

			// then
			Profile updatedProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
			assertThat(updatedProfile.getNickname()).isEqualTo(updateReqDto.nickname());
			assertThat(updatedProfile.getEmail()).isEqualTo(updateReqDto.email());
			assertThat(updatedProfile.getDescription()).isEqualTo(updateReqDto.description());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트")
		public void updateProfileProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String invalidUrl = "invalid_url";
			ProfileUpdateReqDto updateReqDto = new ProfileUpdateReqDto(
				"NewNickname",
				"newEmail@example.com",
				"New description");
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/profiles", invalidUrl)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자가 프로필 수정 시도")
		public void updateProfileNotMember() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext nonMemberAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			ProfileUpdateReqDto updateReqDto = new ProfileUpdateReqDto(
				"NewNickname",
				"newEmail@example.com",
				"New description");
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/profiles", url)
						.cookie(new Cookie("accessToken", nonMemberAuth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}
	}

	@Nested
	@DisplayName("Update Project Profile Role Test")
	class UpdateProjectProfileRoleTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@ParameterizedTest
		@DisplayName("204 - 프로젝트 프로필 역할 수정 성공")
		@ValueSource(strings = {"OWNER", "owner", "Owner"})
		public void updateProjectProfileRoleOwnerSuccess(String role) throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext memberAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile memberProfile = testDataFactory.createProfile(memberAuth.getUser(), project, Role.MEMBER);
			ProfileRoleUpdateReqDto roleUpdateReqDto = new ProfileRoleUpdateReqDto(memberProfile.getId(), role);
			String updateJson = objectMapper.writeValueAsString(roleUpdateReqDto);

			// when
			mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/profiles/role", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getContentAsString();

			// then
			Profile updatedProfile = testDataFactory.findProfileByUserIdAndProjectId(memberAuth.getUser().getId(), project.getId());
			assertThat(updatedProfile.getRole()).isEqualTo(Role.OWNER);
		}

		@ParameterizedTest
		@DisplayName("204 - 프로젝트 프로필 역할 수정 성공")
		@ValueSource(strings = {"MEMBER", "member", "Member"})
		public void updateProjectProfileRoleMemberSuccess(String role) throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext memberAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile memberProfile = testDataFactory.createProfile(memberAuth.getUser(), project, Role.MEMBER);
			ProfileRoleUpdateReqDto roleUpdateReqDto = new ProfileRoleUpdateReqDto(memberProfile.getId(), role);
			String updateJson = objectMapper.writeValueAsString(roleUpdateReqDto);

			// when
			mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/profiles/role", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getContentAsString();

			// then
			Profile updatedProfile = testDataFactory.findProfileByUserIdAndProjectId(memberAuth.getUser().getId(), project.getId());
			assertThat(updatedProfile.getRole()).isEqualTo(Role.MEMBER);
		}

		@Test
		@DisplayName("204 - 프로젝트 Owner가 두명 이상일때 프로필 역할 수정 성공")
		public void updateProjectProfileRoleMemberSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			AuthContext anotherOwnerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			testDataFactory.createProfile(ownerAuth.getUser(), project, Role.OWNER);
			Profile anotherOwnerProfile = testDataFactory.createProfile(anotherOwnerAuth.getUser(), project, Role.OWNER);
			ProfileRoleUpdateReqDto roleUpdateReqDto = new ProfileRoleUpdateReqDto(anotherOwnerProfile.getId(), Role.MEMBER.toString());
			String updateJson = objectMapper.writeValueAsString(roleUpdateReqDto);

			// when
			mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/profiles/role", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getContentAsString();

			// then
			Profile updatedProfile = testDataFactory.findProfileByUserIdAndProjectId(anotherOwnerProfile.getUser().getId(), project.getId());
			assertThat(updatedProfile.getRole()).isEqualTo(Role.MEMBER);
		}


		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트")
		public void updateProfileRoleProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String invalidUrl = "invalid_url";
			ProfileRoleUpdateReqDto roleUpdateReqDto = new ProfileRoleUpdateReqDto(9999L, Role.MEMBER.toString());
			String updateJson = objectMapper.writeValueAsString(roleUpdateReqDto);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/profiles/role", invalidUrl)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}
		
		@Test
		@DisplayName("403 - 프로젝트 오너가 아닌 사용자가 프로필 역할 수정 시도")
		public void updateProfileRoleNotOwner() throws Exception {
			// given
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			AuthContext memberAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());
			Profile memberProfile = testDataFactory.createProfile(memberAuth.getUser(), project, Role.MEMBER);
			ProfileRoleUpdateReqDto roleUpdateReqDto = new ProfileRoleUpdateReqDto(memberProfile.getId(), Role.OWNER.toString());
			String updateJson = objectMapper.writeValueAsString(roleUpdateReqDto);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/profiles/role", url)
						.cookie(new Cookie("accessToken", memberAuth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_OWNER_REQUIRED.getMessage());
		}
		
		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자의 프로필 역할 수정 시도")
		public void updateProfileRoleNotMember() throws Exception {
			// given
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			AuthContext memberAuth = testDataFactory.createAuth(defaultImage);
			AuthContext nonMemberAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());
			Profile memberProfile = testDataFactory.createProfile(memberAuth.getUser(), project, Role.MEMBER);
			ProfileRoleUpdateReqDto roleUpdateReqDto = new ProfileRoleUpdateReqDto(memberProfile.getId(), Role.OWNER.toString());
			String updateJson = objectMapper.writeValueAsString(roleUpdateReqDto);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/profiles/role", url)
						.cookie(new Cookie("accessToken", nonMemberAuth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("400 - 프로젝트의 마지막 오너의 역할을 변경하려는 경우")
		public void updateProfileRoleLastOwner() throws Exception {
			// given
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());
			Profile ownerProfile = testDataFactory.findProfileByUserIdAndProjectId(ownerAuth.getUser().getId(),
				project.getId());
			ProfileRoleUpdateReqDto roleUpdateReqDto = new ProfileRoleUpdateReqDto(ownerProfile.getId(), Role.MEMBER.toString());
			String updateJson = objectMapper.writeValueAsString(roleUpdateReqDto);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/profiles/role", url)
						.cookie(new Cookie("accessToken", ownerAuth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_OWNER_MINIMUM_REQUIRED.getMessage());
		}

		@Test
		@DisplayName("404 - 프로젝트가 참여자가 아닌 프로필의 룰을 변경하는 경우")
		public void updateProfileRoleNotFoundProfile() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			ProfileRoleUpdateReqDto roleUpdateReqDto = new ProfileRoleUpdateReqDto(9999L, Role.MEMBER.toString());
			String updateJson = objectMapper.writeValueAsString(roleUpdateReqDto);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/profiles/role", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_PROFILE_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("400 - 잘못된 역할로 프로필 역할 수정 시도")
		public void updateProfileRoleInvalidRole() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext memberAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile memberProfile = testDataFactory.createProfile(memberAuth.getUser(), project, Role.MEMBER);
			ProfileRoleUpdateReqDto roleUpdateReqDto = new ProfileRoleUpdateReqDto(memberProfile.getId(),
				"INVALID_ROLE");
			String updateJson = objectMapper.writeValueAsString(roleUpdateReqDto);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/profiles/role", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.INVALID_ROLE.getMessage());
		}
	}

	@Nested
	@DisplayName("Update Profile Image Test")
	class UpdateProfileImageTest {

		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - Defafult Image to New Image Success")
		public void updateProfileImageSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile ownerProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
			testDataFactory.setProfileImage(ownerProfile, defaultImage);

			String newImageObjectKey = "new-image-object-key";
			ImageUpdateReqDto imageUpdateReqDto = new ImageUpdateReqDto(newImageObjectKey);
			String request = objectMapper.writeValueAsString(imageUpdateReqDto);

			// when
			mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/profiles/image", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isNoContent());

			// then
			Image afterImage = testDataFactory.findImageById(ownerProfile.getImageId());
			assertThat(afterImage.getObjectKey()).isEqualTo(newImageObjectKey);
			assertThat(afterImage.getId()).isNotEqualTo(TestDataFactory.DEFAULT_IMAGE_ID);
		}

		@Test
		@DisplayName("204 - existing Image to New Image Success")
		public void updateProfileImageNotFoundProfile() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile ownerProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
			Image existingImage = testDataFactory.createImage("http://example.com/existing.png", "existing-object-key");
			testDataFactory.setProfileImage(ownerProfile, existingImage);

			String newImageObjectKey = "new-image-object-key";
			ImageUpdateReqDto imageUpdateReqDto = new ImageUpdateReqDto(newImageObjectKey);
			String request = objectMapper.writeValueAsString(imageUpdateReqDto);

			// when
			mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/profiles/image", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isNoContent());

			// then
			Image afterImage = testDataFactory.findImageById(ownerProfile.getImageId());
			assertThat(afterImage.getObjectKey()).isEqualTo(newImageObjectKey);
			assertThat(afterImage.getId()).isNotEqualTo(existingImage.getId());
		}

		@Test
		@DisplayName("400 - Non ObjectKey Image Update Request")
		public void updateProfileImageBadRequest() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());

			ImageUpdateReqDto imageUpdateReqDto = new ImageUpdateReqDto("");
			String request = objectMapper.writeValueAsString(imageUpdateReqDto);

			// when & then
			mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/profiles/image", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(status().isBadRequest());
		}

	}

	@Nested
	@DisplayName("Delete Project Profile Test")
	class DeleteProjectProfileTest {

		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - Default Image Project Delete Success")
		public void deleteProjectProfileSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile ownerProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
			testDataFactory.setProfileImage(ownerProfile, defaultImage);

			// when
			mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/profiles/image", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());

			// then
			assertThat(ownerProfile.getImageId()).isEqualTo(TestDataFactory.DEFAULT_IMAGE_ID);
		}

		@Test
		@DisplayName("204 - Existing Image Project Delete Success")
		public void deleteProjectProfileWithExistingImageSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile ownerProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
			Image existingImage = testDataFactory.createImage("http://example.com/existing.png", "existing-object-key");
			testDataFactory.setProfileImage(ownerProfile, existingImage);

			// when
			mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/profiles/image", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());

			// then
			assertThat(ownerProfile.getImageId()).isEqualTo(TestDataFactory.DEFAULT_IMAGE_ID);
			assertThat(ownerProfile.getImageId()).isNotEqualTo(existingImage.getId());
		}

	}

}
