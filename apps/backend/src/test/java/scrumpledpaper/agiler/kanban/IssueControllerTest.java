package scrumpledpaper.agiler.kanban;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static scrumpledpaper.agiler.common.TestDataFactory.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import scrumpledpaper.agiler.kanban.dto.IssueDateUpdateReqDto;
import scrumpledpaper.agiler.kanban.dto.IssueDeleteReqDto;
import scrumpledpaper.agiler.kanban.dto.IssueDetailResDto;
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

			KanbanConfig createdKanbanConfig = testDataFactory.findKanbanConfigById(
				createdIssue.getKanbanConfig().getId());
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

			IssueSnapshotDateMapping updatedMapping = testDataFactory.findIssueSnapshotDateMapping(project,
				LocalDate.now());
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

			KanbanConfig createdKanbanConfig = testDataFactory.findKanbanConfigById(
				createdIssue.getKanbanConfig().getId());
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
			Profile anotherMemberProfile = testDataFactory.createProfile(anotherMemberAuth.getUser(), project,
				Role.MEMBER);
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

			KanbanConfig createdKanbanConfig = testDataFactory.findKanbanConfigById(
				createdIssue.getKanbanConfig().getId());
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
		@DisplayName("400 - 시작시간이 종료시간보다 이후인 이슈 생성 요청")
		public void issueCreateBadRequestStartAfterDue() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			testDataFactory.createKanbanConfig(
				project,
				1,
				true,
				false,
				false
			);

			LocalDate today = LocalDate.now();
			LocalDateTime historical = today.atTime(10, 0, 0);
			LocalDateTime future = today.atTime(9, 0, 0);

			IssueCreateReqDto createReqDto = IssueFixture.createIssueCreateReqDto(
				Collections.emptyList(),
				Collections.emptyList(),
				historical,
				future
			);
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/issues", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.ISSUE_INVALID_DATE_RANGE.getMessage());
		}

		@ParameterizedTest
		@MethodSource("provideDateCreateInvalidCases")
		@DisplayName("400 - 시작시간이나 종료시간이 당일이 아닌 경우")
		public void issueUpdateDateBadRequestNotToday(
			Optional<LocalDateTime> startedAt,
			Optional<LocalDateTime> dueAt
		) throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
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
				startedAt.orElse(null),
				dueAt.orElse(null)
			);
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/issues", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.ISSUE_DATE_MUST_TODAY.getMessage());
		}

		private static Stream<Arguments> provideDateCreateInvalidCases() {
			LocalDate today = LocalDate.now();
			LocalDate yesterday = today.minusDays(1);
			LocalDate tomorrow = today.plusDays(1);

			LocalDateTime todayMorning = today.atTime(10, 0, 0);
			LocalDateTime todayEvening = today.atTime(18, 0, 0);
			LocalDateTime yesterdayDateTime = yesterday.atTime(10, 0, 0);
			LocalDateTime tomorrowDateTime = tomorrow.atTime(18, 0, 0);

			return Stream.of(
				// 과거 날짜
				Arguments.of(Optional.of(yesterdayDateTime), Optional.of(todayEvening), "시작일이 과거"),
				Arguments.of(Optional.of(yesterdayDateTime), Optional.of(yesterdayDateTime), "둘 다 과거"),

				// 미래 날짜
				Arguments.of(Optional.of(todayMorning), Optional.of(tomorrowDateTime), "종료일이 미래"),
				Arguments.of(Optional.of(tomorrowDateTime), Optional.of(tomorrowDateTime), "둘 다 미래"),

				// 과거와 미래 혼합
				Arguments.of(Optional.of(yesterdayDateTime), Optional.of(tomorrowDateTime), "시작일 과거, 종료일 미래")
			);
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
			Issue historyIssue = testDataFactory.createIssue(
				project,
				kanbanConfig1,
				Collections.emptyList(),
				Collections.emptyList(),
				true,
				null,
				null
			);
			testDataFactory.updateTimestamps("issue", historyIssue.getId(), LocalDateTime.now().minusDays(10));
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

			assertThat(result.kanbanConfigs())
				.hasSize(2)
				.anySatisfy(config -> {
					assertThat(config.kanbanConfigId()).isEqualTo(kanbanConfig1.getId());
					assertThat(config.statusName()).isEqualTo(kanbanConfig1.getStatusName());
					assertThat(config.priority()).isEqualTo(kanbanConfig1.getPriority());
					assertThat(config.isDefault()).isEqualTo(kanbanConfig1.isDefaultStatus());
				})
				.anySatisfy(config -> {
					assertThat(config.kanbanConfigId()).isEqualTo(kanbanConfig2.getId());
					assertThat(config.statusName()).isEqualTo(kanbanConfig2.getStatusName());
					assertThat(config.priority()).isEqualTo(kanbanConfig2.getPriority());
				});

			assertThat(result.profiles())
				.hasSize(1)
				.first()
				.satisfies(p -> {
					assertThat(p.profileId()).isEqualTo(profile.getId());
					assertThat(p.nickname()).isEqualTo(profile.getNickname());
					assertThat(p.email()).isEqualTo(profile.getEmail());
				});

			assertThat(result.labels())
				.hasSize(2)
				.extracting("name")
				.containsExactlyInAnyOrder(label.getName(), label2.getName());

			assertThat(result.labels())
				.anySatisfy(l -> {
					assertThat(l.labelId()).isEqualTo(label.getId());
					assertThat(l.name()).isEqualTo(label.getName());
					assertThat(l.color()).isEqualTo(label.getColor());
				})
				.anySatisfy(l -> {
					assertThat(l.labelId()).isEqualTo(label2.getId());
					assertThat(l.name()).isEqualTo(label2.getName());
					assertThat(l.color()).isEqualTo(label2.getColor());
				});

			assertThat(result.issues()).hasSize(2);

			assertThat(result.issues())
				.filteredOn(issue -> issue.issueId().equals(issue1.getId()))
				.first()
				.satisfies(issue -> {
					assertThat(issue.title()).isEqualTo(issue1.getTitle());
					assertThat(issue.kanbanConfigId()).isEqualTo(kanbanConfig1.getId());
					assertThat(issue.isDone()).isFalse();

					assertThat(issue.notis())
						.hasSize(1)
						.first()
						.satisfies(noti -> {
							assertThat(noti.notiId()).isEqualTo(subscription.getId());
							assertThat(noti.profileId()).isEqualTo(profile.getId());
						});
				});

			assertThat(result.issues())
				.filteredOn(issue -> issue.issueId().equals(issue2.getId()))
				.first()
				.satisfies(issue -> {
					assertThat(issue.title()).isEqualTo(issue2.getTitle());
					assertThat(issue.kanbanConfigId()).isEqualTo(kanbanConfig2.getId());
					assertThat(issue.isDone()).isFalse();
					assertThat(issue.notis()).isEmpty();
				});
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자가 칸반 보드 및 이슈 조회 요청")
		public void getKanbanBoardAndIssuesForbiddenNotProjectMember() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());
			KanbanConfig kanbanConfig1 = testDataFactory.createKanbanConfig(
				project,
				1,
				true,
				false,
				false
			);
			testDataFactory.createIssue(
				project,
				kanbanConfig1,
				Collections.emptyList(),
				Collections.emptyList(),
				false,
				null,
				null
			);
			LocalDate now = LocalDate.now();

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/issues", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.param("date", now.toString()))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}
	}

	@Nested
	@DisplayName("Get Issue Detail Test")
	class GetIssueDetailTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 이슈 상세 조회 성공")
		public void getIssueDetailSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile profile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
			KanbanConfig kanbanConfig = testDataFactory.createKanbanConfig(
				project,
				1,
				true,
				false,
				false
			);
			Label label1 = testDataFactory.createLabel(project, "label1", "label1 description", "#FF0000");
			Issue issue = testDataFactory.createIssue(
				project,
				kanbanConfig,
				List.of(profile),
				List.of(label1),
				false,
				null,
				null
			);

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/issues/{issueId}", url, issue.getId())
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			IssueDetailResDto result = objectMapper.readValue(response, IssueDetailResDto.class);
			assertThat(result.issueId()).isEqualTo(issue.getId());
			assertThat(result.title()).isEqualTo(issue.getTitle());
			assertThat(result.contents()).isEqualTo(issue.getContents());
			assertThat(result.kanbanConfig().kanbanConfigId()).isEqualTo(kanbanConfig.getId());
			assertThat(result.isDone()).isEqualTo(issue.getIsDone());
			assertThat(result.assignees())
				.hasSize(1)
				.first()
				.satisfies(a -> {
					assertThat(a.profileId()).isEqualTo(profile.getId());
					assertThat(a.nickname()).isEqualTo(profile.getNickname());
					assertThat(a.email()).isEqualTo(profile.getEmail());
				});
			assertThat(result.labels())
				.hasSize(1)
				.first()
				.satisfies(l -> {
					assertThat(l.labelId()).isEqualTo(label1.getId());
					assertThat(l.name()).isEqualTo(label1.getName());
					assertThat(l.description()).isEqualTo(label1.getDescription());
					assertThat(l.color()).isEqualTo(label1.getColor());
				});
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자가 이슈 상세 조회 요청")
		public void getIssueDetailForbiddenNotProjectMember() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());
			KanbanConfig kanbanConfig = testDataFactory.createKanbanConfig(
				project,
				1,
				true,
				false,
				false
			);
			Issue issue = testDataFactory.createIssue(
				project,
				kanbanConfig,
				Collections.emptyList(),
				Collections.emptyList(),
				false,
				null,
				null
			);

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/issues/{issueId}", url, issue.getId())
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 이슈 상세 조회 요청")
		public void getIssueDetailNotFoundIssue() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/issues/{issueId}", url, 9999L)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.ISSUE_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("Update Issue Date Test")
	class UpdateIssueDateTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@ParameterizedTest
		@MethodSource("provideDateUpdateCases")
		@DisplayName("200 - 이슈 시작시간, 종료시간 수정 성공")
		public void issueUpdateDateSuccess(
			Optional<LocalDateTime> startedAt,
			Optional<LocalDateTime> dueAt
		) throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			KanbanConfig defaultKanbanConfig = testDataFactory.createKanbanConfig(
				project, 1, true, false, false
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

			IssueDateUpdateReqDto updateDateReqDto = new IssueDateUpdateReqDto(startedAt, dueAt);

			// when
			String response = mockMvc.perform(
				patch("/api/v1/projects/{projectUrl}/issues/{issueId}/date", url, issue.getId())
					.cookie(new Cookie("accessToken", auth.getToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(updateDateReqDto)))
				.andDo(print())
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// 검증
			Issue updatedIssue = testDataFactory.findIssueById(issue.getId()).get();
			assertThat(response).contains(issue.getId().toString());
			assertThat(updatedIssue.getStartedAt()).isEqualTo(startedAt.orElse(null));
			assertThat(updatedIssue.getDueAt()).isEqualTo(dueAt.orElse(null));
		}

		private static Stream<Arguments> provideDateUpdateCases() {
			LocalDate today = LocalDate.now();
			LocalDateTime historical = today.atTime(10, 0, 0);
			LocalDateTime future = today.atTime(18, 0, 0);

			return Stream.of(
				Arguments.of(Optional.of(historical), Optional.of(future), "둘 다 있음"),
				Arguments.of(Optional.of(historical), Optional.empty(), "시작일만 있음"),
				Arguments.of(Optional.empty(), Optional.of(future), "종료일만 있음"),
				Arguments.of(Optional.empty(), Optional.empty(), "둘 다 없음")
			);
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자가 이슈 시작시간, 종료시간 수정 요청")
		public void issueUpdateDateForbiddenNotProjectMember() throws Exception {
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

			LocalDate today = LocalDate.now();
			LocalDateTime historical = today.atTime(10, 0, 0);
			LocalDateTime future = today.atTime(18, 0, 0);

			IssueDateUpdateReqDto updateDateReqDto = new IssueDateUpdateReqDto(
				Optional.of(historical),
				Optional.of(future)
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues/{issueId}/date", url, issue.getId())
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateDateReqDto)))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 이슈의 ID로 시작시간, 종료시간 수정 요청")
		public void issueUpdateDateNotFoundIssue() throws Exception {
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

			LocalDate today = LocalDate.now();
			LocalDateTime historical = today.atTime(10, 0, 0);
			LocalDateTime future = today.atTime(18, 0, 0);

			IssueDateUpdateReqDto updateDateReqDto = new IssueDateUpdateReqDto(
				Optional.of(historical),
				Optional.of(future)
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues/{issueId}/date", url, 9999L)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateDateReqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.ISSUE_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 이슈 시작시간, 종료시간 수정 요청")
		public void issueUpdateDateNotFoundProject() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);

			LocalDate today = LocalDate.now();
			LocalDateTime historical = today.atTime(10, 0, 0);
			LocalDateTime future = today.atTime(18, 0, 0);

			IssueDateUpdateReqDto updateDateReqDto = new IssueDateUpdateReqDto(
				Optional.of(historical),
				Optional.of(future)
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues/{issueId}/date", "not_exist_url", 9999L)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateDateReqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("400 - 종료시간이 시작시간보다 이전인 경우")
		public void issueUpdateDateBadRequestDueAtBeforeStartedAt() throws Exception {
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

			LocalDate today = LocalDate.now();
			LocalDateTime historical = today.atTime(10, 0, 0);
			LocalDateTime future = today.atTime(9, 0, 0);

			IssueDateUpdateReqDto updateDateReqDto = new IssueDateUpdateReqDto(
				Optional.of(historical),
				Optional.of(future)
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues/{issueId}/date", url, issue.getId())
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateDateReqDto)))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.ISSUE_INVALID_DATE_RANGE.getMessage());
		}

		@ParameterizedTest
		@MethodSource("provideDateUpdateInvalidCases")
		@DisplayName("400 - 시작시간이나 종료시간이 당일이 아닌 경우")
		public void issueUpdateDateBadRequestNotToday(
			Optional<LocalDateTime> startedAt,
			Optional<LocalDateTime> dueAt
		) throws Exception {
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

			IssueDateUpdateReqDto updateDateReqDto = new IssueDateUpdateReqDto(startedAt, dueAt);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/issues/{issueId}/date", url, issue.getId())
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateDateReqDto)))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.ISSUE_DATE_MUST_TODAY.getMessage());
		}

		private static Stream<Arguments> provideDateUpdateInvalidCases() {
			LocalDate today = LocalDate.now();
			LocalDate yesterday = today.minusDays(1);
			LocalDate tomorrow = today.plusDays(1);

			LocalDateTime todayMorning = today.atTime(10, 0, 0);
			LocalDateTime todayEvening = today.atTime(18, 0, 0);
			LocalDateTime yesterdayDateTime = yesterday.atTime(10, 0, 0);
			LocalDateTime tomorrowDateTime = tomorrow.atTime(18, 0, 0);

			return Stream.of(
				// 과거 날짜
				Arguments.of(Optional.of(yesterdayDateTime), Optional.of(todayEvening), "시작일이 과거"),
				Arguments.of(Optional.of(yesterdayDateTime), Optional.of(yesterdayDateTime), "둘 다 과거"),

				// 미래 날짜
				Arguments.of(Optional.of(todayMorning), Optional.of(tomorrowDateTime), "종료일이 미래"),
				Arguments.of(Optional.of(tomorrowDateTime), Optional.of(tomorrowDateTime), "둘 다 미래"),

				// 과거와 미래 혼합
				Arguments.of(Optional.of(yesterdayDateTime), Optional.of(tomorrowDateTime), "시작일 과거, 종료일 미래")
			);
		}
	}
}
