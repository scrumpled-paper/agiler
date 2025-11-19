package scrumpledpaper.agiler.kanban;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import scrumpledpaper.agiler.kanban.dto.IssueCreateReqDto;
import scrumpledpaper.agiler.kanban.entity.Issue;
import scrumpledpaper.agiler.kanban.entity.IssueLabel;
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
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile assigneeProfile = testDataFactory.createProfile(assigneeAuth.getUser(), project, Role.MEMBER);
			testDataFactory.createKanbanConfig(project, "To do", 1, true, false, false);
			Label label1 = testDataFactory.createLabel(project, "label1", "label1 description", "#FF0000");
			Label label2 = testDataFactory.createLabel(project, "label2", "label2 description", "#00FF00");
			IssueCreateReqDto createReqDto = IssueFixture.createIssueCreateReqDto(
				"issue title",
				"issue contents",
				assigneeProfile.getId(),
				java.util.List.of(label1.getId(), label2.getId()),
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
			assertThat(createdIssue.getProfile().getId()).isEqualTo(assigneeProfile.getId());

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
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 이슈 생성 요청")
		public void issueCreateNotFoundProject() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			IssueCreateReqDto createReqDto = IssueFixture.createIssueCreateReqDto(
				"issue title",
				"issue contents",
				1L,
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
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());
			testDataFactory.createKanbanConfig(
				project,
				"To do",
				1,
				true,
				false,
				false
			);
			IssueCreateReqDto createReqDto = IssueFixture.createIssueCreateReqDto(
				"issue title",
				"issue contents",
				1L,
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
		@DisplayName("403 - 존재하지 않는 Assignee 프로필로 이슈 생성 요청")
		public void issueCreateNotFoundAssigneeProfile() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			testDataFactory.createKanbanConfig(
				project,
				"To do",
				1,
				true,
				false,
				false);
			IssueCreateReqDto createReqDto = IssueFixture.createIssueCreateReqDto(
				"issue title",
				"issue contents",
				9999L,
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
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile assigneeProfile = testDataFactory.createProfile(assigneeAuth.getUser(), project, Role.MEMBER);
			IssueCreateReqDto createReqDto = IssueFixture.createIssueCreateReqDto(
				"issue title",
				"issue contents",
				assigneeProfile.getId(),
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
}
