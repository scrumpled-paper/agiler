package scrumpledpaper.agiler.kanban;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.kanban.dto.IssueDeleteReqDto;
import scrumpledpaper.agiler.kanban.entity.Issue;
import scrumpledpaper.agiler.kanban.entity.IssueLabel;
import scrumpledpaper.agiler.kanban.entity.IssueProfile;
import scrumpledpaper.agiler.kanban.entity.IssueSnapshotDateMapping;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.kanban.entity.Label;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;

@IntegrationTest
@Transactional
public class SnapshotControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private TestDataFactory testDataFactory;
	Image defaultImage;

	@Nested
	@DisplayName("Create Snapshot For Today Test")
	class CreateSnapshotForTodayTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 전날 생성된 이슈들로 오늘 스냅샷 생성 성공")
		public void createSnapshotForTodaySuccess_AfterSixAM() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile ownerProfile = testDataFactory.findProfileByUserIdAndProjectId(
				auth.getUser().getId(), project.getId());
			List<KanbanConfig> kanbanConfigs = testDataFactory.defaultKanbanConfigSet(project);
			Label label1 = testDataFactory.createLabel(project, "name", "des", "#FFFFFF");
			Label label2 = testDataFactory.createLabel(project, "name2", "des2", "#000000");

			KanbanConfig defaultConfig = kanbanConfigs.stream()
				.filter(KanbanConfig::isDefaultStatus)
				.findFirst()
				.orElseThrow();

			KanbanConfig backlogConfig = kanbanConfigs.stream()
				.filter(KanbanConfig::isBacklog)
				.findFirst()
				.orElseThrow();

			KanbanConfig doneConfig = kanbanConfigs.stream()
				.filter(KanbanConfig::getIsDone)
				.findFirst()
				.orElseThrow();

			Issue defaultConfigIssue = testDataFactory.createIssue(
				project, defaultConfig, List.of(ownerProfile), List.of(label1, label2), false, null, null);
			Issue backlogConfigIssue = testDataFactory.createIssue(
				project, backlogConfig, Collections.emptyList(), Collections.emptyList(), false, null, null);
			Issue doneConfigIssue = testDataFactory.createIssue(
				project, doneConfig, List.of(ownerProfile), List.of(label2), true, null, null);

			// 전날 생성 된 이슈
			LocalDateTime issueCreatedAt = LocalDateTime.now()
				.minusDays(1)
				.withMinute(0)
				.withSecond(0)
				.withNano(0);
			// 스냅샷은 오늘로 생성되어야 함
			LocalDate snapshotDate = LocalDate.now();

			testDataFactory.updateTimestamps("issue", defaultConfigIssue.getId(), issueCreatedAt);
			testDataFactory.updateTimestamps("issue", backlogConfigIssue.getId(), issueCreatedAt);
			testDataFactory.updateTimestamps("issue", doneConfigIssue.getId(), issueCreatedAt);
			List<Issue> allIssues = testDataFactory.findIssuesByProjectId(project.getId());

			// when
			mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/snapshots", url)
						.cookie(new Cookie("accessToken", auth.getToken())))
				.andExpect(status().isOk());

			// then
			verifySnapshotCreation(project, allIssues, snapshotDate, defaultConfigIssue, backlogConfigIssue,
				doneConfigIssue, backlogConfig);
		}

		@Test
		@DisplayName("200 - 어제가 아닌 이슈들로 오늘 스냅샷 생성 성공")
		public void createSnapshotForTodaySuccess_notYesterdayIssues() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile ownerProfile = testDataFactory.findProfileByUserIdAndProjectId(
				auth.getUser().getId(), project.getId());
			List<KanbanConfig> kanbanConfigs = testDataFactory.defaultKanbanConfigSet(project);
			Label label1 = testDataFactory.createLabel(project, "name", "des", "#FFFFFF");
			Label label2 = testDataFactory.createLabel(project, "name2", "des2", "#000000");

			KanbanConfig defaultConfig = kanbanConfigs.stream()
				.filter(KanbanConfig::isDefaultStatus)
				.findFirst()
				.orElseThrow();

			KanbanConfig backlogConfig = kanbanConfigs.stream()
				.filter(KanbanConfig::isBacklog)
				.findFirst()
				.orElseThrow();

			KanbanConfig doneConfig = kanbanConfigs.stream()
				.filter(KanbanConfig::getIsDone)
				.findFirst()
				.orElseThrow();

			Issue defaultConfigIssue = testDataFactory.createIssue(
				project, defaultConfig, List.of(ownerProfile), List.of(label1, label2), false, null, null);
			Issue backlogConfigIssue = testDataFactory.createIssue(
				project, backlogConfig, Collections.emptyList(), Collections.emptyList(), false, null, null);
			Issue doneConfigIssue = testDataFactory.createIssue(
				project, doneConfig, List.of(ownerProfile), List.of(label2), true, null, null);

			// 2일 전 기준 시간 전에 생성 된 이슈
			LocalDateTime issueCreatedAt = LocalDateTime.now()
				.minusDays(2)
				.withMinute(0)
				.withSecond(0)
				.withNano(0);
			// 스냅샷은 오늘로 생성되어야 함
			LocalDate snapshotDate = LocalDate.now();

			testDataFactory.updateTimestamps("issue", defaultConfigIssue.getId(), issueCreatedAt);
			testDataFactory.updateTimestamps("issue", backlogConfigIssue.getId(), issueCreatedAt);
			testDataFactory.updateTimestamps("issue", doneConfigIssue.getId(), issueCreatedAt);
			List<Issue> allIssues = testDataFactory.findIssuesByProjectId(project.getId());

			// when
			mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/snapshots", url)
						.cookie(new Cookie("accessToken", auth.getToken())))
				.andExpect(status().isOk());

			// then
			verifySnapshotCreation(project, allIssues, snapshotDate, defaultConfigIssue, backlogConfigIssue,
				doneConfigIssue, backlogConfig);
		}

		// 검증 로직 공통 메서드
		private void verifySnapshotCreation(Project project, List<Issue> issues, LocalDate snapshotDate,
			Issue defaultConfigIssue, Issue backlogConfigIssue,
			Issue doneConfigIssue, KanbanConfig backlogConfig) {
			// 1. IssueSnapshotDateMapping 검증
			IssueSnapshotDateMapping mapping = testDataFactory.findIssueSnapshotDateMapping(project, snapshotDate);
			assertThat(mapping).isNotNull();
			long count = issues.stream()
				.filter(issue -> !issue.getIsDone())
					.count();
			assertThat(mapping.getIssueCount()).isEqualTo(count);

			// 2. 전체 이슈 개수 검증
			List<Issue> allIssues = testDataFactory.findIssuesByProjectId(project.getId());
			assertThat(allIssues.size()).isEqualTo(issues.size() + count);

			// 3. 원본 이슈 ID 목록
			List<Long> originalIssueIds = List.of(
				defaultConfigIssue.getId(),
				backlogConfigIssue.getId(),
				doneConfigIssue.getId()
			);

			// 4. 복사된 backlog 이슈들 검증
			List<Issue> copiedBacklogIssues = allIssues.stream()
				.filter(issue -> issue.getKanbanConfig().isBacklog())
				.filter(issue -> !originalIssueIds.contains(issue.getId()))
				.toList();
			assertThat(copiedBacklogIssues).hasSize(2);

			// 5. 원본 이슈 매핑
			Map<String, Issue> originalIssuesMap = Map.of(
				defaultConfigIssue.getTitle(), defaultConfigIssue,
				backlogConfigIssue.getTitle(), backlogConfigIssue
			);

			// 6. 각 복사된 이슈 상세 검증
			for (Issue copiedIssue : copiedBacklogIssues) {
				Issue originalIssue = originalIssuesMap.get(copiedIssue.getTitle());
				assertThat(originalIssue).isNotNull();

				assertThat(copiedIssue.getTitle()).isEqualTo(originalIssue.getTitle());
				assertThat(copiedIssue.getContents()).isEqualTo(originalIssue.getContents());
				assertThat(copiedIssue.getIsDone()).isEqualTo(originalIssue.getIsDone());
				assertThat(copiedIssue.getKanbanConfig().getId()).isEqualTo(backlogConfig.getId());
				assertThat(copiedIssue.getId()).isNotEqualTo(originalIssue.getId());

				// IssueProfile 복사 검증
				List<IssueProfile> originalProfiles = testDataFactory.findIssueProfilesByIssueId(originalIssue.getId());
				List<IssueProfile> copiedProfiles = testDataFactory.findIssueProfilesByIssueId(copiedIssue.getId());
				assertThat(copiedProfiles).hasSameSizeAs(originalProfiles);

				List<Long> originalProfileIds = originalProfiles.stream()
					.map(ip -> ip.getProfile().getId()).sorted().toList();
				List<Long> copiedProfileIds = copiedProfiles.stream()
					.map(ip -> ip.getProfile().getId()).sorted().toList();
				assertThat(copiedProfileIds).isEqualTo(originalProfileIds);

				// IssueLabel 복사 검증
				List<IssueLabel> originalLabels = testDataFactory.findIssueLabelsByIssueId(originalIssue.getId());
				List<IssueLabel> copiedLabels = testDataFactory.findIssueLabelsByIssueId(copiedIssue.getId());
				assertThat(copiedLabels).hasSameSizeAs(originalLabels);

				List<Long> originalLabelIds = originalLabels.stream()
					.map(il -> il.getLabel().getId()).sorted().toList();
				List<Long> copiedLabelIds = copiedLabels.stream()
					.map(il -> il.getLabel().getId()).sorted().toList();
				assertThat(copiedLabelIds).isEqualTo(originalLabelIds);
			}
		}

		@Test
		@DisplayName("200 - 이슈가 없는 경우")
		public void createSnapshotForTodayNoIssues() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());

			// when
			mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/snapshots", url)
						.cookie(new Cookie("accessToken", auth.getToken())))
				.andExpect(status().isOk());

			// then
			IssueSnapshotDateMapping mapping = testDataFactory.findIssueSnapshotDateMapping(project, LocalDate.now());
			assertThat(mapping).isNull();
		}

		@Test
		@DisplayName("200 - 이미 오늘 스냅샷이 존재하는 경우")
		public void createSnapshotForTodayAlreadyExists() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());

			testDataFactory.createIssueSnapshotDateMapping(
				project,
				5,
				LocalDate.now()
			);

			// when
			mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/snapshots", url)
						.cookie(new Cookie("accessToken", auth.getToken())))
				.andExpect(status().isOk());

			// then
			IssueSnapshotDateMapping mapping = testDataFactory.findIssueSnapshotDateMapping(project, LocalDate.now());
			assertThat(mapping).isNotNull();
			assertThat(mapping.getIssueCount()).isEqualTo(5);
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 스냅샷 생성 요청")
		public void createSnapshotForTodayNotFoundProject() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/snapshots", "not_exist_url")
						.cookie(new Cookie("accessToken", auth.getToken())))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자가 스냅샷 생성 요청")
		public void createSnapshotForTodayForbiddenNotProjectMember() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/snapshots", url)
						.cookie(new Cookie("accessToken", auth.getToken())))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}
	}

	@Nested
	@DisplayName("Issue delete Snapshot Count Down Test")
	class IssueDeleteSnapshotCountDownTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - 이슈 삭제로 인한 당일 스냅샷 카운트 다운으로 인한 삭제 성공")
		public void issueDeleteSnapshotCountDownSuccessPastIssue() throws Exception {
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

			LocalDate snapshotDate = LocalDate.now();
			testDataFactory.createIssueSnapshotDateMapping(
				project,
				1,
				snapshotDate
			);

			IssueDeleteReqDto deleteReqDto = new IssueDeleteReqDto(issue.getId());

			// when
			mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/issues", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(deleteReqDto)))
				.andExpect(status().isNoContent());

			// then - 카운트가 0이 되어 매핑 삭제됨
			IssueSnapshotDateMapping mapping = testDataFactory.findIssueSnapshotDateMapping(project, snapshotDate);
			assertThat(mapping).isNull();
		}

		@Test
		@DisplayName("204 - 이슈 삭제로 인한 오늘의 스냅샷 카운트 다운 성공")
		public void issueDeleteSnapshotCountDownSuccessNotDeletePastIssue() throws Exception {
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
			Issue issue1 = testDataFactory.createIssue(
				project,
				defaultKanbanConfig,
				List.of(profile),
				List.of(label),
				false,
				null,
				null
			);
			Issue issue2 = testDataFactory.createIssue(
				project,
				defaultKanbanConfig,
				List.of(profile),
				List.of(label),
				false,
				null,
				null
			);

			LocalDate snapshotDate = LocalDate.now();

			testDataFactory.createIssueSnapshotDateMapping(
				project,
				2,
				snapshotDate
			);

			IssueDeleteReqDto deleteReqDto = new IssueDeleteReqDto(issue1.getId());

			// when
			mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/issues", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(deleteReqDto)))
				.andExpect(status().isNoContent());

			// then - 카운트가 1이 되어 매핑 유지됨
			IssueSnapshotDateMapping mapping = testDataFactory.findIssueSnapshotDateMapping(project, snapshotDate);
			assertThat(mapping).isNotNull();
			assertThat(mapping.getIssueCount()).isEqualTo(1);
		}

		@Test
		@DisplayName("204 - 어제 이슈 삭제 시 어제의 스냅샷 카운트 다운")
		public void issueDeleteYesterdayIssueNoCountDown() throws Exception {
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
			Issue yesterdayIssue = testDataFactory.createIssue(
				project,
				defaultKanbanConfig,
				List.of(profile),
				List.of(label),
				false,
				null,
				null
			);
			LocalDateTime issueCreatedAt = LocalDateTime.now().minusDays(1);
			testDataFactory.updateTimestamps("issue", yesterdayIssue.getId(), issueCreatedAt);
			IssueSnapshotDateMapping yesterdayMapping = testDataFactory.createIssueSnapshotDateMapping(
				project,
				2,
				LocalDate.now().minusDays(1)
			);
			IssueSnapshotDateMapping todayMapping = testDataFactory.createIssueSnapshotDateMapping(
				project,
				2,
				LocalDate.now()
			);


			IssueDeleteReqDto deleteReqDto = new IssueDeleteReqDto(yesterdayIssue.getId());

			// when - 현재 시간 상관없이 어제 이슈를 삭제
			mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/issues", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(deleteReqDto)))
				.andExpect(status().isNoContent());

			// then
			IssueSnapshotDateMapping fetchedYesterdayMapping = testDataFactory.findIssueSnapshotDateMapping(
				project, yesterdayMapping.getSnapshotDate());
			assertThat(fetchedYesterdayMapping).isNotNull();
			assertThat(fetchedYesterdayMapping.getIssueCount()).isEqualTo(1);
			IssueSnapshotDateMapping fetchedTodayMapping = testDataFactory.findIssueSnapshotDateMapping(
				project, todayMapping.getSnapshotDate());
			assertThat(fetchedTodayMapping).isNotNull();
		}
	}

	@Nested
	@DisplayName("Get Available Snapshot Dates Test")
	class GetAvailableSnapshotDatesTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 스냅샷 날짜 목록 조회 성공")
		public void getAvailableSnapshotDatesSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());

			YearMonth currentYearMonth = YearMonth.now();
			LocalDate firstDay = currentYearMonth.atDay(1);
			LocalDate fifteenthDay = currentYearMonth.atDay(15);
			LocalDate twentyEighthDay = currentYearMonth.atDay(28);

			testDataFactory.createIssueSnapshotDateMapping(project, 3, firstDay);
			testDataFactory.createIssueSnapshotDateMapping(project, 5, fifteenthDay);
			testDataFactory.createIssueSnapshotDateMapping(project, 2, twentyEighthDay);

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/snapshots", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.param("year", String.valueOf(currentYearMonth.getYear()))
						.param("month", String.valueOf(currentYearMonth.getMonthValue())))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(firstDay.toString());
			assertThat(response).contains(fifteenthDay.toString());
			assertThat(response).contains(twentyEighthDay.toString());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트의 스냅샷 날짜 목록 조회 요청")
		public void getAvailableSnapshotDatesNotFoundProject() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			YearMonth currentYearMonth = YearMonth.now();

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/snapshots", "not_exist_url")
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.param("year", String.valueOf(currentYearMonth.getYear()))
						.param("month", String.valueOf(currentYearMonth.getMonthValue())))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자의 스냅샷 날짜 목록 조회 요청")
		public void getAvailableSnapshotDatesForbiddenNotProjectMember() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());
			YearMonth currentYearMonth = YearMonth.now();

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/snapshots", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.param("year", String.valueOf(currentYearMonth.getYear()))
						.param("month", String.valueOf(currentYearMonth.getMonthValue())))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}
	}
}
