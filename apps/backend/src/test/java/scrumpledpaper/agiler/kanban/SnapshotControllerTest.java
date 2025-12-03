package scrumpledpaper.agiler.kanban;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import scrumpledpaper.agiler.annotation.IntegrationTest;
import scrumpledpaper.agiler.common.AuthContext;
import scrumpledpaper.agiler.common.TestDataFactory;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.image.entity.Image;
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
		@DisplayName("200 - 스냅샷 생성 성공")
		public void createSnapshotForTodaySuccess() throws Exception {
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
				project,
				defaultConfig,
				List.of(ownerProfile),
				List.of(label1, label2),
				false,
				null,
				null
			);
			Issue backlogConfigIssue = testDataFactory.createIssue(
				project,
				backlogConfig,
				Collections.emptyList(),
				Collections.emptyList(),
				false,
				null,
				null
			);
			Issue doneConfigIssue = testDataFactory.createIssue(
				project,
				doneConfig,
				List.of(ownerProfile),
				List.of(label2),
				true,
				null,
				null
			);

			// when
			mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/snapshots/today", url)
						.cookie(new Cookie("accessToken", auth.getToken())))
				.andExpect(status().isOk());

			// then
			LocalDate today = LocalDate.now();

			// 1. IssueSnapshotDateMapping 검증
			IssueSnapshotDateMapping mapping = testDataFactory.findIssueSnapshotDateMapping(project, today);
			assertThat(mapping).isNotNull();
			assertThat(mapping.getIssueCount()).isEqualTo(3); // 원본 이슈 3개

			// 2. 전체 이슈 개수 검증 (원본 3개, 복사된 backlog 2개 추가 = 총 5개)
			List<Issue> issues = testDataFactory.findIssuesByProjectId(project.getId());
			assertThat(issues).hasSize(5);

			// 3. 원본 이슈 ID 목록
			List<Long> originalIssueIds = List.of(
				defaultConfigIssue.getId(),
				backlogConfigIssue.getId(),
				doneConfigIssue.getId()
			);

			// 4. 복사된 backlog 이슈들 검증 (isDone = false인 이슈 2개만 복사됨)
			List<Issue> copiedBacklogIssues = issues.stream()
				.filter(issue -> issue.getKanbanConfig().isBacklog())
				.filter(issue -> !originalIssueIds.contains(issue.getId()))
				.toList();
			assertThat(copiedBacklogIssues).hasSize(2);

			// 5. 원본 이슈 매핑 (복사 대상인 isDone = false 이슈만)
			Map<String, Issue> originalIssuesMap = Map.of(
				defaultConfigIssue.getTitle(), defaultConfigIssue,
				backlogConfigIssue.getTitle(), backlogConfigIssue
			);

			// 6. 각 복사된 이슈 상세 검증
			for (Issue copiedIssue : copiedBacklogIssues) {
				Issue originalIssue = originalIssuesMap.get(copiedIssue.getTitle());
				assertThat(originalIssue).isNotNull();

				// 기본 필드 검증
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
					.map(ip -> ip.getProfile().getId())
					.sorted()
					.toList();
				List<Long> copiedProfileIds = copiedProfiles.stream()
					.map(ip -> ip.getProfile().getId())
					.sorted()
					.toList();
				assertThat(copiedProfileIds).isEqualTo(originalProfileIds);

				// IssueLabel 복사 검증
				List<IssueLabel> originalLabels = testDataFactory.findIssueLabelsByIssueId(originalIssue.getId());
				List<IssueLabel> copiedLabels = testDataFactory.findIssueLabelsByIssueId(copiedIssue.getId());
				assertThat(copiedLabels).hasSameSizeAs(originalLabels);

				List<Long> originalLabelIds = originalLabels.stream()
					.map(il -> il.getLabel().getId())
					.sorted()
					.toList();
				List<Long> copiedLabelIds = copiedLabels.stream()
					.map(il -> il.getLabel().getId())
					.sorted()
					.toList();
				assertThat(copiedLabelIds).isEqualTo(originalLabelIds);
			}
		}

		@Test
		@DisplayName("200 - 이미 스냅샷이 있는 경우")
		public void createSnapshotForTodayAlreadyExists() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());

			LocalDate today = LocalDate.now();
			testDataFactory.createIssueSnapshotDateMapping(project, 5, today, LocalDate.now().atStartOfDay());

			// when
			mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/snapshots/today", url)
						.cookie(new Cookie("accessToken", auth.getToken())))
				.andExpect(status().isOk());

			// then
			IssueSnapshotDateMapping mapping = testDataFactory.findIssueSnapshotDateMapping(project, today);
			assertThat(mapping).isNotNull();
			assertThat(mapping.getIssueCount()).isEqualTo(5);
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
					post("/api/v1/projects/{projectUrl}/snapshots/today", url)
						.cookie(new Cookie("accessToken", auth.getToken())))
				.andExpect(status().isOk());

			// then
			LocalDate today = LocalDate.now();
			IssueSnapshotDateMapping mapping = testDataFactory.findIssueSnapshotDateMapping(project, today);
			assertThat(mapping).isNull();
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 스냅샷 생성 요청")
		public void createSnapshotForTodayNotFoundProject() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/snapshots/today", "not_exist_url")
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
					post("/api/v1/projects/{projectUrl}/snapshots/today", url)
						.cookie(new Cookie("accessToken", auth.getToken())))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}
	}
}
