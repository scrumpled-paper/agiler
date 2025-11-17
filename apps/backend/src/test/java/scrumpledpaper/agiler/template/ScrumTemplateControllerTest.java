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
import scrumpledpaper.agiler.template.dto.ScrumTemplateCreateReqDto;
import scrumpledpaper.agiler.template.dto.ScrumTemplateUpdateReqDto;
import scrumpledpaper.agiler.template.entity.ScrumTemplate;

@IntegrationTest
@Transactional
public class ScrumTemplateControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private TestDataFactory testDataFactory;
	Image defaultImage;

	@Nested
	@DisplayName("Create Scrum Template")
	class CreateScrumTemplate {
		@BeforeEach
		void setUp() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - 스크럼 생성 템플릿 생성 성공")
		public void scrumTemplateCreateSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			ScrumTemplateCreateReqDto createReqDto = new ScrumTemplateCreateReqDto(
				"스크럼 템플릿 제목",
				"스크럼 템플릿 설명",
				"스크럼 템플릿 내용"
			);
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/scrums/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getContentAsString();

			// then
			List<ScrumTemplate> scrumTemplates = testDataFactory.findScrumTemplatesByProjectId(project.getId());
			assertThat(scrumTemplates)
				.anyMatch(scrumTemplate ->
					scrumTemplate.getTitle().equals(createReqDto.title()) &&
					scrumTemplate.getDescription().equals(createReqDto.description()) &&
					scrumTemplate.getContents().equals(createReqDto.contents())
				);
		}

		@Test
		@DisplayName("403 - 멤버가 아닌 사용자가 스크럼 생성 템플릿 생성 시도")
		public void scrumTemplateCreateForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());
			ScrumTemplateCreateReqDto createReqDto = new ScrumTemplateCreateReqDto(
				"스크럼 템플릿 제목",
				"스크럼 템플릿 설명",
				"스크럼 템플릿 내용"
			);
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/scrums/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 스크럼 템플릿 생성 시도")
		public void scrumTemplateCreateProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non_existing_url";
			ScrumTemplateCreateReqDto createReqDto = new ScrumTemplateCreateReqDto(
				"스크럼 템플릿 제목",
				"스크럼 템플릿 설명",
				"스크럼 템플릿 내용"
			);
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/scrums/templates", url)
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
	@DisplayName("update scrum template")
	class UpdateScrumTemplate {
		private ScrumTemplate existingTemplate;

		@BeforeEach
		void setUp() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - 스크럼 생성 템플릿 수정 성공")
		public void scrumTemplateUpdateSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			existingTemplate = testDataFactory.createScrumTemplate(
				project,
				"스크럼",
				"스크럼 템플릿",
				"Old Template"
			);
			ScrumTemplateUpdateReqDto updateReqDto = new ScrumTemplateUpdateReqDto(
				existingTemplate.getId(),
				"update template",
				"update scrum template",
				"Updated Template"
			);
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/scrums/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getContentAsString();

			// then
			ScrumTemplate updatedTemplate = testDataFactory.findScrumTemplateById(existingTemplate.getId());
			assertThat(updatedTemplate.getTitle()).isEqualTo(updateReqDto.title());
			assertThat(updatedTemplate.getDescription()).isEqualTo(updateReqDto.description());
			assertThat(updatedTemplate.getContents()).isEqualTo(updateReqDto.contents());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 스크럼 템플릿 수정 시도")
		public void scrumTemplateUpdateTemplateNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			ScrumTemplateUpdateReqDto updateReqDto = new ScrumTemplateUpdateReqDto(
				9999L,
				"update template",
				"update scrum template",
				"Updated Template"
			);
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/scrums/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.SCRUM_TEMPLATE_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("403 - 멤버가 아닌 사용자가 스크럼 생성 템플릿 수정 시도")
		public void scrumTemplateUpdateForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			existingTemplate = testDataFactory.createScrumTemplate(
				project,
				"스크럼",
				"스크럼 템플릿",
				"Old Template"
			);
			ScrumTemplateUpdateReqDto updateReqDto = new ScrumTemplateUpdateReqDto(
				9999L,
				"update template",
				"update scrum template",
				"Updated Template"
			);
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/scrums/templates", url)
						.cookie(new Cookie("accessToken", ownerAuth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 스크럼 템플릿 수정 시도")
		public void scrumTemplateUpdateProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non_existing_url";
			ScrumTemplateUpdateReqDto updateReqDto = new ScrumTemplateUpdateReqDto(
				9999L,
				"update template",
				"update scrum template",
				"Updated Template"
			);
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/scrums/templates", url)
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
	@DisplayName("get scrum template list")
	class GetScrumTemplateList {
		@BeforeEach
		void setUp() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 스크럼 생성 템플릿 리스트 조회 성공")
		public void getScrumTemplateListSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			ScrumTemplate template1 = testDataFactory.createScrumTemplate(
				project,
				"스크럼",
				"스크럼 템플릿",
				"Template 1"
			);
			ScrumTemplate template2 = testDataFactory.createScrumTemplate(
				project,
				"데일리",
				"데일리 스크럼 템플릿",
				"Template 2"
			);

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/scrums/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(template1.getTitle());
			assertThat(response).contains(template2.getTitle());
		}

		@Test
		@DisplayName("403 - 멤버가 아닌 사용자가 스크럼 생성 템플릿 리스트 조회 시도")
		public void getScrumTemplateListForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/scrums/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}
	}

}
