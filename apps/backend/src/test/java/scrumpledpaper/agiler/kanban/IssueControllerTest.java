package scrumpledpaper.agiler.kanban;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static scrumpledpaper.agiler.common.TestDataFactory.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import scrumpledpaper.agiler.annotation.IntegrationTest;
import scrumpledpaper.agiler.common.AuthContext;
import scrumpledpaper.agiler.common.TestDataFactory;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.fixture.IssueFixture;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.kanban.dto.IssueAssigneesReqDto;
import scrumpledpaper.agiler.kanban.dto.IssueCreateReqDto;
import scrumpledpaper.agiler.kanban.dto.IssueDeleteReqDto;
import scrumpledpaper.agiler.kanban.dto.IssueUpdateReqDto;
import scrumpledpaper.agiler.kanban.entity.Issue;
import scrumpledpaper.agiler.kanban.entity.IssueLabel;
import scrumpledpaper.agiler.kanban.entity.IssueProfile;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.kanban.entity.Label;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.entity.Role;

@IntegrationTest
@Transactional
public class IssueControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private TestDataFactory testDataFactory;
	Image defaultImage;

	@Nested
	@DisplayName("Create Issue Test")
	class CreateIssueTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("201 - 이슈 생성 성공")
		public void issueCreateSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext assigneeAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile assigneeProfile = testDataFactory.createProfile(assigneeAuth.getUser(), project, Role.MEMBER);
			KanbanConfig defaultKanbanConfig = testDataFactory.createKanbanConfig(project, 1, true, false, false);
			Label label1 = testDataFactory.createLabel(project, "label1", "label1 description", "#FF0000");
			Label label2 = testDataFactory.createLabel(project, "label2", "label2 description", "#00FF00");
			IssueCreateReqDto createReqDto = IssueFixture.createIssueCreateReqDto(
				List.of(assigneeProfile.getId()),
				List.of(label1.getId(), label2.getId()),
				null,
				null
			);
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/issues", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

			// then
			Issue createdIssue = testDataFactory.findIssueByProjectId(project.getId());
			assertThat(createdIssue.getTitle()).isEqualTo(createReqDto.title());
			assertThat(createdIssue.getContents()).isEqualTo(createReqDto.contents());

			KanbanConfig createdKanbanConfig = testDataFactory.findKanbanConfigById(createdIssue.getKanbanConfig().getId());
			assertThat(createdKanbanConfig.getId()).isEqualTo(defaultKanbanConfig.getId());

			List<IssueLabel> createdIssueLabels = testDataFactory.findIssueLabelsByIssueId(createdIssue.getId());
			assertThat(createdIssueLabels).hasSize(2);
			assertThat(createdIssueLabels)
				.extracting(
					issueLabel -> issueLabel.getLabel().getId(),
					issueLabel -> issueLabel.getLabel().getName(),
					issueLabel -> issueLabel.getIssue().getId()
				)
				.containsExactlyInAnyOrder(
					tuple(label1.getId(), label1.getName(), createdIssue.getId()),
					tuple(label2.getId(), label2.getName(), createdIssue.getId())
				);

			List<IssueProfile> createdIssueProfiles = testDataFactory.findIssueProfilesByIssueId(createdIssue.getId());
			assertThat(createdIssueProfiles).hasSize(1);
			assertThat(createdIssueProfiles)
				.extracting(
					issueProfile -> issueProfile.getIssue().getId(),
					issueProfile -> issueProfile.getProfile().getId()
				)
				.containsExactlyInAnyOrder(
					tuple(createdIssue.getId(), assigneeProfile.getId())
				);
		}

		@Test
		@DisplayName("201 - 이슈 생성 성공 - Assignees가 여러명이며, 이 중 프로젝트 멤버가 아닌 사용자가 포함된 경우")
		public void IssueCreateWithNonMemberAssigneesTest() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext assigneeAuth = testDataFactory.createAuth(defaultImage);
			AuthContext anotherMemberAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile assigneeProfile = testDataFactory.createProfile(assigneeAuth.getUser(), project, Role.MEMBER);
			Profile anotherMemberProfile = testDataFactory.createProfile(anotherMemberAuth.getUser(), project, Role.MEMBER);
			KanbanConfig defaultKanbanConfig = testDataFactory.createKanbanConfig(project, 1, true, false, false);
			Label label1 = testDataFactory.createLabel(project, "label1", "label1 description", "#FF0000");
			Label label2 = testDataFactory.createLabel(project, "label2", "label2 description", "#00FF00");
			IssueCreateReqDto createReqDto = IssueFixture.createIssueCreateReqDto(
				List.of(assigneeProfile.getId(), anotherMemberProfile.getId(), 9999L),
				List.of(label1.getId(), label2.getId()),
				null,
				null
			);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/issues", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createReqDto)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

			// then
			Issue createdIssue = testDataFactory.findIssueByProjectId(project.getId());
			assertThat(createdIssue.getTitle()).isEqualTo(createReqDto.title());
			assertThat(createdIssue.getContents()).isEqualTo(createReqDto.contents());

			KanbanConfig createdKanbanConfig = testDataFactory.findKanbanConfigById(createdIssue.getKanbanConfig().getId());
			assertThat(createdKanbanConfig.getId()).isEqualTo(defaultKanbanConfig.getId());

			List<IssueLabel> createdIssueLabels = testDataFactory.findIssueLabelsByIssueId(createdIssue.getId());
			assertThat(createdIssueLabels).hasSize(2);
			assertThat(createdIssueLabels)
				.extracting(
					issueLabel -> issueLabel.getLabel().getId(),
					issueLabel -> issueLabel.getLabel().getName(),
					issueLabel -> issueLabel.getIssue().getId()
				)
				.containsExactlyInAnyOrder(
					tuple(label1.getId(), label1.getName(), createdIssue.getId()),
					tuple(label2.getId(), label2.getName(), createdIssue.getId())
				);

			List<IssueProfile> createdIssueProfiles = testDataFactory.findIssueProfilesByIssueId(createdIssue.getId());
			assertThat(createdIssueProfiles).hasSize(2);
			assertThat(createdIssueProfiles)
				.extracting(
					issueProfile -> issueProfile.getIssue().getId(),
					issueProfile -> issueProfile.getProfile().getId()
				)
				.containsExactlyInAnyOrder(
					tuple(createdIssue.getId(), assigneeProfile.getId()),
					tuple(createdIssue.getId(), anotherMemberProfile.getId())
				);

			assertThat(response).contains(createdIssue.getId().toString());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 이슈 생성 요청")
		public void issueCreateNotFoundProject() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			IssueCreateReqDto createReqDto = IssueFixture.createIssueCreateReqDto(
				Collections.emptyList(),
				Collections.emptyList(),
				null,
				null
			);
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/issues", "not_exist_url")
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자가 이슈 생성 요청")
		public void issueCreateForbiddenNotProjectMember() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());
			testDataFactory.createKanbanConfig(
				project,
				1,
				true,
				false,
				false
			);
			IssueCreateReqDto createReqDto = IssueFixture.createIssueCreateReqDto(
				Collections.emptyList(),
				Collections.emptyList(),
				null,
				null
			);
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/issues", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 디폴트 칸반 설정이 없는 상태에서 이슈 생성 요청")
		public void issueCreateNotFoundDefaultKanbanConfig() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext assigneeAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile assigneeProfile = testDataFactory.createProfile(assigneeAuth.getUser(), project, Role.MEMBER);
			IssueCreateReqDto createReqDto = IssueFixture.createIssueCreateReqDto(
				List.of(assigneeProfile.getId()),
				Collections.emptyList(),
				null,
				null
			);
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/issues", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.DEFAULT_KANBAN_CONFIG_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("Update Issue Test")
	class UpdateIssueTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 이슈 수정 성공")
		public void issueUpdateSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			KanbanConfig defaultKanbanConfig = testDataFactory.createKanbanConfig(
				project,
				1,
				true,
				false,
				false
			);
			Issue issue = testDataFactory.createIssue(
				project,
				defaultKanbanConfig,
				Collections.emptyList(),
				Collections.emptyList(),
				false,
				null,
				null
			);
			IssueUpdateReqDto updateReqDto = new IssueUpdateReqDto(
				issue.getId(),
				randomString(20),
				randomString(50),
				null,
				null
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateReqDto)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			Issue updatedIssue = testDataFactory.findIssueById(issue.getId()).get();
			assertThat(updatedIssue.getTitle()).isEqualTo(updateReqDto.title());
			assertThat(updatedIssue.getContents()).isEqualTo(updateReqDto.contents());

			assertThat(response).contains(issue.getId().toString());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 이슈 수정 요청")
		public void issueUpdateNotFoundProject() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			IssueUpdateReqDto updateReqDto = new IssueUpdateReqDto(
				9999L,
				randomString(20),
				randomString(50),
				null,
				null
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues", "not_exist_url")
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateReqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자가 이슈 수정 요청")
		public void issueUpdateForbiddenNotProjectMember() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());
			KanbanConfig defaultKanbanConfig = testDataFactory.createKanbanConfig(
				project,
				1,
				true,
				false,
				false
			);
			Issue issue = testDataFactory.createIssue(
				project,
				defaultKanbanConfig,
				Collections.emptyList(),
				Collections.emptyList(),
				false,
				null,
				null
			);
			IssueUpdateReqDto updateReqDto = new IssueUpdateReqDto(
				issue.getId(),
				randomString(20),
				randomString(50),
				null,
				null
				);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateReqDto)))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}
	}

	@Nested
	@DisplayName("Delete Issue Test")
	class DeleteIssueTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - 이슈 삭제 성공")
		public void issueDeleteSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile profile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
			KanbanConfig defaultKanbanConfig = testDataFactory.createKanbanConfig(
				project,
				1,
				true,
				false,
				false
			);
			Label label = testDataFactory.createLabel(project, "label1", "label1 description", "#FF0000");
			Issue issue = testDataFactory.createIssue(
				project,
				defaultKanbanConfig,
				List.of(profile),
				List.of(label),
				false,
				null,
				null
			);
			IssueDeleteReqDto deleteReqDto = new IssueDeleteReqDto(issue.getId());

			// when
			mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/issues", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(deleteReqDto)))
				.andExpect(status().isNoContent());

			// then
			assertThat(testDataFactory.findIssueById(issue.getId())).isEmpty();
			assertThat(testDataFactory.findIssueLabelsByIssueId(issue.getId())).isEmpty();
			assertThat(testDataFactory.findIssueProfilesByIssueId(issue.getId())).isEmpty();
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 이슈 삭제 요청")
		public void issueDeleteNotFoundProject() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			IssueDeleteReqDto deleteReqDto = new IssueDeleteReqDto(9999L);

			// when
			String response = mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/issues", "not_exist_url")
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(deleteReqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자가 이슈 삭제 요청")
		public void issueDeleteForbiddenNotProjectMember() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());
			KanbanConfig defaultKanbanConfig = testDataFactory.createKanbanConfig(
				project,
				1,
				true,
				false,
				false
			);
			Issue issue = testDataFactory.createIssue(
				project,
				defaultKanbanConfig,
				Collections.emptyList(),
				Collections.emptyList(),
				false,
				null,
				null
			);
			IssueDeleteReqDto deleteReqDto = new IssueDeleteReqDto(issue.getId());

			// when
			String response = mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/issues", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(deleteReqDto)))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 이슈 삭제 요청")
		public void issueDeleteNotFoundIssue() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			KanbanConfig defaultKanbanConfig = testDataFactory.createKanbanConfig(
				project,
				1,
				true,
				false,
				false
			);
			IssueDeleteReqDto deleteReqDto = new IssueDeleteReqDto(9999L);

			// when
			String response = mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/issues", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(deleteReqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.ISSUE_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("Update Issue Assignees Test")
	class UpdateIssueAssigneesTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - 이슈 담당자 수정 성공")
		public void issueUpdateAssigneesSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext assigneeAuth = testDataFactory.createAuth(defaultImage);
			AuthContext deleteAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile authProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
			Profile deleteProfile = testDataFactory.createProfile(deleteAuth.getUser(), project, Role.MEMBER);
			Profile assigneeProfile = testDataFactory.createProfile(assigneeAuth.getUser(), project, Role.MEMBER);
			KanbanConfig defaultKanbanConfig = testDataFactory.createKanbanConfig(
				project,
				1,
				true,
				false,
				false
			);
			Issue issue = testDataFactory.createIssue(
				project,
				defaultKanbanConfig,
				List.of(deleteProfile),
				Collections.emptyList(),
				false,
				null,
				null
			);
			IssueAssigneesReqDto updateAssigneesReqDto = new IssueAssigneesReqDto(
				List.of(authProfile.getId(), assigneeProfile.getId())
			);

			// when
			mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues/{issueId}/assignees", url, issue.getId())
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateAssigneesReqDto)))
				.andExpect(status().isNoContent());

			// then
			List<IssueProfile> updatedIssueProfiles = testDataFactory.findIssueProfilesByIssueId(issue.getId());
			assertThat(updatedIssueProfiles).hasSize(2);
			assertThat(updatedIssueProfiles)
				.extracting(
					issueProfile -> issueProfile.getIssue().getId(),
					issueProfile -> issueProfile.getProfile().getId()
				)
				.containsExactlyInAnyOrder(
					tuple(issue.getId(), authProfile.getId()),
					tuple(issue.getId(), assigneeProfile.getId())
				);

			assertThat(updatedIssueProfiles)
				.extracting(issueProfile -> issueProfile.getProfile().getId())
				.doesNotContain(deleteProfile.getId());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 이슈에 담당자 수정 요청")
		public void issueUpdateAssigneesNotFoundProject() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			IssueAssigneesReqDto updateAssigneesReqDto = new IssueAssigneesReqDto(
				Collections.emptyList()
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues/{issueId}/assignees", url, 9999L)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateAssigneesReqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.ISSUE_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자가 이슈 담당자 수정 요청")
		public void issueUpdateAssigneesForbiddenNotProjectMember() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());
			KanbanConfig defaultKanbanConfig = testDataFactory.createKanbanConfig(
				project,
				1,
				true,
				false,
				false
			);
			Issue issue = testDataFactory.createIssue(
				project,
				defaultKanbanConfig,
				Collections.emptyList(),
				Collections.emptyList(),
				false,
				null,
				null
			);
			IssueAssigneesReqDto updateAssigneesReqDto = new IssueAssigneesReqDto(
				Collections.emptyList()
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues/{issueId}/assignees", url, issue.getId())
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateAssigneesReqDto)))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 이슈 담당자 수정 요청")
		public void issueUpdateAssigneesNotFoundIssue() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			IssueAssigneesReqDto updateAssigneesReqDto = new IssueAssigneesReqDto(
				Collections.emptyList()
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues/{issueId}/assignees", "not_exist_url", 9999L)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateAssigneesReqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}
	}
}
