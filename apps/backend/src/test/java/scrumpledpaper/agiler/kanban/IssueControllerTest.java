package scrumpledpaper.agiler.kanban;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static scrumpledpaper.agiler.common.TestDataFactory.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

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
import scrumpledpaper.agiler.kanban.dto.IssueKanbanConfigUpdateReqDto;
import scrumpledpaper.agiler.kanban.dto.IssueLabelsReqDto;
import scrumpledpaper.agiler.kanban.dto.IssueUpdateReqDto;
import scrumpledpaper.agiler.kanban.dto.KanbanBoardResDto;
import scrumpledpaper.agiler.kanban.entity.Issue;
import scrumpledpaper.agiler.kanban.entity.IssueLabel;
import scrumpledpaper.agiler.kanban.entity.IssueProfile;
import scrumpledpaper.agiler.kanban.entity.IssueSnapshotDateMapping;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.kanban.entity.Label;
import scrumpledpaper.agiler.notification.domain.NotificationSubscription;
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
			int issueCount = 2;
			IssueSnapshotDateMapping mapping = testDataFactory.createIssueSnapshotDateMapping(
				project,
				issueCount,
				LocalDate.now()
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

			IssueSnapshotDateMapping updatedMapping = testDataFactory.findIssueSnapshotDateMapping(project, LocalDate.now());
			assertThat(updatedMapping.getIssueCount()).isEqualTo(issueCount + 1);
		}

		@Test
		@DisplayName("201 - 라벨과 어사인 없는 이슈 생성 성공")
		public void issueCreateWithoutLabelsAndAssigneesSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url-no-label-assignee";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			KanbanConfig defaultKanbanConfig = testDataFactory.createKanbanConfig(project, 1, true, false, false);

			IssueCreateReqDto createReqDto = IssueFixture.createIssueCreateReqDto(
				List.of(),
				List.of(),
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
			assertThat(createdIssueLabels).isEmpty();

			List<IssueProfile> createdIssueProfiles = testDataFactory.findIssueProfilesByIssueId(createdIssue.getId());
			assertThat(createdIssueProfiles).isEmpty();
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
		@DisplayName("200 - 이슈 담당자 수정 성공")
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
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues/{issueId}/assignees", url, issue.getId())
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateAssigneesReqDto)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

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

			assertThat(response).contains(issue.getId().toString());
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

	@Nested
	@DisplayName("Update Issue Labels Test")
	class UpdateIssueLabelsTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 이슈 라벨 수정 성공")
		public void issueUpdateLabelsSuccess() throws Exception {
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
			Label label1 = testDataFactory.createLabel(project, "label1", "label1 description", "#FF0000");
			Label label2 = testDataFactory.createLabel(project, "label2", "label2 description", "#00FF00");
			Label label3 = testDataFactory.createLabel(project, "label3", "label3 description", "#0000FF");
			Issue issue = testDataFactory.createIssue(
				project,
				defaultKanbanConfig,
				Collections.emptyList(),
				List.of(label1),
				false,
				null,
				null
			);
			IssueLabelsReqDto updateLabelsReqDto = new IssueLabelsReqDto(
				List.of(label2.getId(), label3.getId())
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues/{issueId}/labels", url, issue.getId())
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateLabelsReqDto)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			List<IssueLabel> updatedIssueLabels = testDataFactory.findIssueLabelsByIssueId(issue.getId());
			assertThat(updatedIssueLabels).hasSize(2);
			assertThat(updatedIssueLabels)
				.extracting(
					issueLabel -> issueLabel.getLabel().getId(),
					issueLabel -> issueLabel.getLabel().getName(),
					issueLabel -> issueLabel.getIssue().getId()
				)
				.containsExactlyInAnyOrder(
					tuple(label2.getId(), label2.getName(), issue.getId()),
					tuple(label3.getId(), label3.getName(), issue.getId())
				);

			assertThat(updatedIssueLabels)
				.extracting(issueLabel -> issueLabel.getLabel().getId())
				.doesNotContain(label1.getId());

			assertThat(response).contains(issue.getId().toString());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 이슈에 라벨 수정 요청")
		public void issueUpdateLabelsNotFoundIssue() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			IssueLabelsReqDto updateLabelsReqDto = new IssueLabelsReqDto(
				Collections.emptyList()
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues/{issueId}/labels", url, 9999L)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateLabelsReqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.ISSUE_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자가 이슈 라벨 수정 요청")
		public void issueUpdateLabelsForbiddenNotProjectMember() throws Exception {
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
			IssueLabelsReqDto updateLabelsReqDto = new IssueLabelsReqDto(
				Collections.emptyList()
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues/{issueId}/labels", url, issue.getId())
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateLabelsReqDto)))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 이슈 라벨 수정 요청")
		public void issueUpdateLabelsNotFoundProject() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			IssueLabelsReqDto updateLabelsReqDto = new IssueLabelsReqDto(
				Collections.emptyList()
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues/{issueId}/labels", "not_exist_url", 9999L)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateLabelsReqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("Update Issue Kanban Config Test")
	class UpdateIssueKanbanConfigTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 이슈 칸반 설정 수정 성공")
		public void issueUpdateKanbanConfigSuccess() throws Exception {
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
			KanbanConfig newKanbanConfig = testDataFactory.createKanbanConfig(
				project,
				2,
				false,
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
			IssueKanbanConfigUpdateReqDto updateKanbanConfigReqDto = new IssueKanbanConfigUpdateReqDto(
				newKanbanConfig.getId()
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues/{issueId}/kanban-config", url, issue.getId())
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateKanbanConfigReqDto)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			Issue updatedIssue = testDataFactory.findIssueById(issue.getId()).get();
			assertThat(updatedIssue.getKanbanConfig().getId()).isEqualTo(newKanbanConfig.getId());
			assertThat(response).contains(issue.getId().toString());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 이슈에 칸반 설정 수정 요청")
		public void issueUpdateKanbanConfigNotFoundIssue() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			KanbanConfig kanbanConfig = testDataFactory.createKanbanConfig(
				project,
				1,
				true,
				false,
				false
			);
			IssueKanbanConfigUpdateReqDto updateKanbanConfigReqDto = new IssueKanbanConfigUpdateReqDto(
				kanbanConfig.getId()
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues/{issueId}/kanban-config", url, 9999L)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateKanbanConfigReqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.ISSUE_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자가 이슈 칸반 설정 수정 요청")
		public void issueUpdateKanbanConfigForbiddenNotProjectMember() throws Exception {
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
			IssueKanbanConfigUpdateReqDto updateKanbanConfigReqDto = new IssueKanbanConfigUpdateReqDto(
				defaultKanbanConfig.getId()
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues/{issueId}/kanban-config", url, issue.getId())
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateKanbanConfigReqDto)))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 이슈 칸반 설정 수정 요청")
		public void issueUpdateKanbanConfigNotFoundProject() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			IssueKanbanConfigUpdateReqDto updateKanbanConfigReqDto = new IssueKanbanConfigUpdateReqDto(
				9999L
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues/{issueId}/kanban-config", "not_exist_url", 9999L)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateKanbanConfigReqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 칸반 설정으로 수정 요청")
		public void issueUpdateKanbanConfigNotFoundKanbanConfig() throws Exception {
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
			IssueKanbanConfigUpdateReqDto updateKanbanConfigReqDto = new IssueKanbanConfigUpdateReqDto(
				9999L
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues/{issueId}/kanban-config", url, issue.getId())
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateKanbanConfigReqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.KANBAN_CONFIG_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("Get Kanban Board And Issues Test")
	class GetKanbanBoardAndIssuesTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 칸반 보드 및 이슈 조회 성공")
		public void getKanbanBoardAndIssuesSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile profile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
			Label label = testDataFactory.createLabel(project, "label1", "label1 description", "#FF0000");
			Label label2 = testDataFactory.createLabel(project, "label2", "label2 description", "#00FF00");
			KanbanConfig kanbanConfig1 = testDataFactory.createKanbanConfig(
				project,
				1,
				true,
				false,
				false
			);
			KanbanConfig kanbanConfig2 = testDataFactory.createKanbanConfig(
				project,
				2,
				false,
				false,
				false
			);
			Issue issue1 = testDataFactory.createIssue(
				project,
				kanbanConfig1,
				Collections.emptyList(),
				Collections.emptyList(),
				false,
				null,
				null
			);
			Issue issue2 = testDataFactory.createIssue(
				project,
				kanbanConfig2,
				Collections.emptyList(),
				Collections.emptyList(),
				false,
				null,
				null
			);
			NotificationSubscription subscription = testDataFactory.createNotificationSubscription(
				auth.getUser(),
				profile,
				issue1,
				kanbanConfig1.getId(),
				kanbanConfig2.getId()
			);

			LocalDate now = LocalDate.now();
			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/issues", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.param("date", now.toString()))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			KanbanBoardResDto result = objectMapper.readValue(response, KanbanBoardResDto.class);

			assertThat(result.kanbanConfigs()).isNotEmpty();
			assertThat(result.kanbanConfigs().get(0).statusName()).isEqualTo(kanbanConfig1.getStatusName());
			assertThat(result.kanbanConfigs().get(0).priority()).isEqualTo((kanbanConfig1.getPriority()));
			assertThat(result.kanbanConfigs().get(0).isDefault()).isEqualTo((kanbanConfig1.isDefaultStatus()));

			assertThat(result.profiles()).isNotEmpty();
			assertThat(result.profiles().get(0).profileId()).isNotNull();
			assertThat(result.profiles().get(0).nickname()).isEqualTo(profile.getNickname());

			assertThat(result.labels()).isNotEmpty();
			assertThat(result.labels().get(0).labelId()).isNotNull();
			assertThat(result.labels().get(0).name()).isEqualTo(label.getName());
			assertThat(result.labels().get(1).labelId()).isNotNull();
			assertThat(result.labels().get(1).name()).isEqualTo(label2.getName());

			assertThat(result.issues()).isNotEmpty();
			assertThat(result.issues().get(0).issueId()).isEqualTo((issue1.getId()));
			assertThat(result.issues().get(1).issueId()).isEqualTo((issue2.getId()));
			assertThat(result.issues().get(0).notis().get(0).notiId()).isEqualTo((subscription.getId()));
			assertThat(result.issues().get(0).notis().get(0).profileId()).isNotNull();
		}
	}
}
