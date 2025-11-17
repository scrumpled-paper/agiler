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
import scrumpledpaper.agiler.template.dto.ScrumTemplateCreateReqDto;
import scrumpledpaper.agiler.template.entity.IssueTemplate;
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
			String response = mockMvc.perform(
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
		@DisplayName("403 - 멤버가 아닌 사용자가 스크럼 템플릿 생성 시도")
		public void scrumTemplateCreateFailByForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			testDataFactory.createProject(url);
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
		public void scrumTemplateCreateFailByNotFoundProject() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "not_exist_url";
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
}
