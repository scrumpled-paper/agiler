package scrumpledpaper.agiler.kanban;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import scrumpledpaper.agiler.annotation.IntegrationTest;
import scrumpledpaper.agiler.common.AuthContext;
import scrumpledpaper.agiler.common.TestDataFactory;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.kanban.dto.IssueStatusUpdateReqDto;
import scrumpledpaper.agiler.kanban.entity.Issue;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@Transactional
class IssueIntegrationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private TestDataFactory testDataFactory;

	private AuthContext auth;

	@BeforeEach
	void setUp() {
		Image image = testDataFactory.createDefaultImage();
		auth = testDataFactory.createAuth(image);
	}

	private Cookie getAuthCookie() {
		return new Cookie("accessToken", auth.getToken());
	}

	@Test
	@DisplayName("이슈 상태 변경이 정상적으로 수행된다.")
	void changeIssueStatus_Success() throws Exception {
		// given
		String anyProjectUrl = "test-project";
		Project project = testDataFactory.createProjectAndOwnerProfile(anyProjectUrl, auth.getUser());
		Profile profile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
		KanbanConfig todoKanbanConfig = testDataFactory.createKanbanConfig(project, "TODO", 2, false, false, false);
		KanbanConfig doneKanbanConfig = testDataFactory.createKanbanConfig(project, "DONE", 2, false, false, false);
		Issue issue = testDataFactory.createIssue(todoKanbanConfig, profile, "Test Issue for Subscription", false, "test", LocalDateTime.now(), LocalDateTime.now());
		testDataFactory.createProfileNotificationChannel(auth.getUser(), profile, "SLACK", "https://hooks.slack.test/webhook");
		testDataFactory.createNotificationSubscription(auth.getUser(), profile, issue, todoKanbanConfig.getId(), doneKanbanConfig.getId());
		IssueStatusUpdateReqDto request = new IssueStatusUpdateReqDto(todoKanbanConfig.getId(), doneKanbanConfig.getId());

		// when
		ResultActions result = mockMvc.perform(patch("/api/v1/projects/{projectUrl}/issues/{issueId}/status", anyProjectUrl, issue.getId())
				.cookie(getAuthCookie())
				.content(objectMapper.writeValueAsString(request))
				.contentType("application/json"));

		// then
		result.andExpect(status().isOk());
		Issue updatedIssue = testDataFactory.findIssueById(issue.getId());
		assertThat(updatedIssue.getId()).isEqualTo(issue.getId());
		assertThat(updatedIssue.getKanbanConfig().getStatusName()).isEqualTo("DONE");
	}

}
