package scrumpledpaper.agiler.template;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.template.dto.IssueTemplateCreateReqDto;
import scrumpledpaper.agiler.template.dto.IssueTemplateUpdateReqDto;
import scrumpledpaper.agiler.template.entity.IssueTemplate;

@IntegrationTest
@Transactional
public class IssueTemplateControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private TestDataFactory testDataFactory;
	Image defaultImage;

	@Nested
	@DisplayName("create issue template")
	class CreateIssueTemplate {
		@BeforeEach
		void setUp() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - 이슈 생성 템플릿 생성 성공")
		public void issueCreateSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			IssueTemplateCreateReqDto createReqDto = new IssueTemplateCreateReqDto(
				"버그",
				"버그 이슈 템플릿",
				"Test Template"
			);
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/issues/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getContentAsString();

			// then
			List<IssueTemplate> issueTemplates = testDataFactory.findIssueTemplatesByProjectId(project.getId());
			assert(issueTemplates.stream().anyMatch(issueTemplate ->
				issueTemplate.getTitle().equals(createReqDto.title()) &&
				issueTemplate.getDescription().equals(createReqDto.description()) &&
				issueTemplate.getContents().equals(createReqDto.contents())
			));
		}

		@Test
		@DisplayName("403 - 멤버가 아닌 사용자가 이슈 생성 템플릿 생성 시도")
		public void issueCreateForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			testDataFactory.createProjectAndOwnerProfile(url, testDataFactory.createAuth(defaultImage).getUser());
			IssueTemplateCreateReqDto createReqDto = new IssueTemplateCreateReqDto(
				"버그",
				"버그 이슈 템플릿",
				"Test Template"
			);
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/issues/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 이슈 템플릿 생성 시도")
		public void issueCreateProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non_existing_url";
			IssueTemplateCreateReqDto createReqDto = new IssueTemplateCreateReqDto(
				"버그",
				"버그 이슈 템플릿",
				"Test Template"
			);
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/issues/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("update issue template")
	class UpdateIssueTemplate {
		private IssueTemplate existingTemplate;

		@BeforeEach
		void setUp() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - 이슈 생성 템플릿 수정 성공")
		public void issueUpdateSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			existingTemplate = testDataFactory.createIssueTemplate(
				project,
				"버그",
				"버그 이슈 템플릿",
				"Old Template"
			);
			IssueTemplateUpdateReqDto updateReqDto = new IssueTemplateUpdateReqDto(
				existingTemplate.getId(),
				"update template",
				"update issue template",
				"Updated Template"
			);
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/issues/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getContentAsString();

			// then
			IssueTemplate updatedTemplate = testDataFactory.findIssueTemplateById(existingTemplate.getId());
			assertThat(updatedTemplate.getTitle()).isEqualTo(updateReqDto.title());
			assertThat(updatedTemplate.getDescription()).isEqualTo(updateReqDto.description());
			assertThat(updatedTemplate.getContents()).isEqualTo(updateReqDto.contents());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 이슈 템플릿 수정 시도")
		public void issueUpdateTemplateNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			IssueTemplateUpdateReqDto updateReqDto = new IssueTemplateUpdateReqDto(
				9999L,
				"update template",
				"update issue template",
				"Updated Template"
			);
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/issues/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.ISSUE_TEMPLATE_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("403 - 멤버가 아닌 사용자가 이슈 생성 템플릿 수정 시도")
		public void issueUpdateForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			existingTemplate = testDataFactory.createIssueTemplate(
				project,
				"버그",
				"버그 이슈 템플릿",
				"Old Template"
			);
			IssueTemplateUpdateReqDto updateReqDto = new IssueTemplateUpdateReqDto(
				9999L,
				"update template",
				"update issue template",
				"Updated Template"
			);
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/issues/templates", url)
						.cookie(new Cookie("accessToken", ownerAuth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 이슈 템플릿 수정 시도")
		public void issueUpdateProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non_existing_url";
			IssueTemplateUpdateReqDto updateReqDto = new IssueTemplateUpdateReqDto(
				9999L,
				"update template",
				"update issue template",
				"Updated Template"
			);
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/issues/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("get issue template list")
	class GetIssueTemplateList {
		@BeforeEach
		void setUp() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 이슈 생성 템플릿 리스트 조회 성공")
		public void getIssueTemplateListSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			IssueTemplate template1 = testDataFactory.createIssueTemplate(
				project,
				"버그",
				"버그 이슈 템플릿",
				"Template 1"
			);
			IssueTemplate template2 = testDataFactory.createIssueTemplate(
				project,
				"기능",
				"기능 이슈 템플릿",
				"Template 2"
			);

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/issues/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(template1.getTitle());
			assertThat(response).contains(template2.getTitle());
		}

		@Test
		@DisplayName("403 - 멤버가 아닌 사용자가 이슈 생성 템플릿 리스트 조회 시도")
		public void getIssueTemplateListForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			testDataFactory.createProjectAndOwnerProfile(url, testDataFactory.createAuth(defaultImage).getUser());

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/issues/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}
	}
}
