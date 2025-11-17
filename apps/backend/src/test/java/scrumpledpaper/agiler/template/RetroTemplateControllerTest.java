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
import scrumpledpaper.agiler.template.dto.RetroTemplateCreateReqDto;
import scrumpledpaper.agiler.template.dto.RetroTemplateDeleteReqDto;
import scrumpledpaper.agiler.template.dto.RetroTemplateUpdateReqDto;
import scrumpledpaper.agiler.template.entity.RetroTemplate;

@IntegrationTest
@Transactional
public class RetroTemplateControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private TestDataFactory testDataFactory;
	Image defaultImage;

	@Nested
	@DisplayName("Create Retro Template")
	class CreateRetroTemplate {
		@BeforeEach
		void setUp() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - 회고 생성 템플릿 생성 성공")
		public void retroTemplateCreateSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			RetroTemplateCreateReqDto createReqDto = new RetroTemplateCreateReqDto(
				"회고 템플릿 제목",
				"회고 템플릿 설명",
				"회고 템플릿 내용"
			);
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/retros/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getContentAsString();

			// then
			List<RetroTemplate> retroTemplates = testDataFactory.findRetroTemplatesByProjectId(project.getId());
			assertThat(retroTemplates)
				.anyMatch(retroTemplate ->
					retroTemplate.getTitle().equals(createReqDto.title()) &&
					retroTemplate.getDescription().equals(createReqDto.description()) &&
					retroTemplate.getContents().equals(createReqDto.contents())
				);
		}

		@Test
		@DisplayName("403 - 멤버가 아닌 사용자가 회고 생성 템플릿 생성 시도")
		public void retroTemplateCreateForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());
			RetroTemplateCreateReqDto createReqDto = new RetroTemplateCreateReqDto(
				"회고 템플릿 제목",
				"회고 템플릿 설명",
				"회고 템플릿 내용"
			);
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/retros/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 회고 템플릿 생성 시도")
		public void retroTemplateCreateProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non_existing_url";
			RetroTemplateCreateReqDto createReqDto = new RetroTemplateCreateReqDto(
				"회고 템플릿 제목",
				"회고 템플릿 설명",
				"회고 템플릿 내용"
			);
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/retros/templates", url)
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
	@DisplayName("update retro template")
	class UpdateRetroTemplate {
		private RetroTemplate existingTemplate;

		@BeforeEach
		void setUp() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - 회고 생성 템플릿 수정 성공")
		public void retroTemplateUpdateSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			existingTemplate = testDataFactory.createRetroTemplate(
				project,
				"회고",
				"회고 템플릿",
				"Old Template"
			);
			RetroTemplateUpdateReqDto updateReqDto = new RetroTemplateUpdateReqDto(
				existingTemplate.getId(),
				"update template",
				"update retro template",
				"Updated Template"
			);
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/retros/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getContentAsString();

			// then
			RetroTemplate updatedTemplate = testDataFactory.findRetroTemplateById(existingTemplate.getId());
			assertThat(updatedTemplate.getTitle()).isEqualTo(updateReqDto.title());
			assertThat(updatedTemplate.getDescription()).isEqualTo(updateReqDto.description());
			assertThat(updatedTemplate.getContents()).isEqualTo(updateReqDto.contents());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 회고 템플릿 수정 시도")
		public void retroTemplateUpdateTemplateNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			RetroTemplateUpdateReqDto updateReqDto = new RetroTemplateUpdateReqDto(
				9999L,
				"update template",
				"update retro template",
				"Updated Template"
			);
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/retros/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.RETRO_TEMPLATE_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("403 - 멤버가 아닌 사용자가 회고 생성 템플릿 수정 시도")
		public void retroTemplateUpdateForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			existingTemplate = testDataFactory.createRetroTemplate(
				project,
				"회고",
				"회고 템플릿",
				"Old Template"
			);
			RetroTemplateUpdateReqDto updateReqDto = new RetroTemplateUpdateReqDto(
				9999L,
				"update template",
				"update retro template",
				"Updated Template"
			);
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/retros/templates", url)
						.cookie(new Cookie("accessToken", ownerAuth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 회고 템플릿 수정 시도")
		public void retroTemplateUpdateProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non_existing_url";
			RetroTemplateUpdateReqDto updateReqDto = new RetroTemplateUpdateReqDto(
				9999L,
				"update template",
				"update retro template",
				"Updated Template"
			);
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/retros/templates", url)
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
	@DisplayName("get retro template list")
	class GetRetroTemplateList {
		@BeforeEach
		void setUp() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 회고 생성 템플릿 리스트 조회 성공")
		public void getRetroTemplateListSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			RetroTemplate template1 = testDataFactory.createRetroTemplate(
				project,
				"회고",
				"회고 템플릿",
				"Template 1"
			);
			RetroTemplate template2 = testDataFactory.createRetroTemplate(
				project,
				"스프린트",
				"스프린트 회고 템플릿",
				"Template 2"
			);

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/retros/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(template1.getTitle());
			assertThat(response).contains(template2.getTitle());
		}

		@Test
		@DisplayName("403 - 멤버가 아닌 사용자가 회고 생성 템플릿 리스트 조회 시도")
		public void getRetroTemplateListForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/retros/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}
	}

	@Nested
	@DisplayName("get retro template detail")
	class GetRetroTemplateDetail {
		@BeforeEach
		void setUp() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 회고 생성 템플릿 상세 조회 성공")
		public void getRetroTemplateDetailSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			RetroTemplate template = testDataFactory.createRetroTemplate(
				project,
				"회고",
				"회고 템플릿",
				"Template Detail"
			);

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/retros/templates/{templateId}", url, template.getId())
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(template.getTitle());
			assertThat(response).contains(template.getDescription());
			assertThat(response).contains(template.getContents());
		}

		@Test
		@DisplayName("403 - 멤버가 아닌 사용자가 회고 생성 템플릿 상세 조회 시도")
		public void getRetroTemplateDetailForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url,	auth.getUser());
			RetroTemplate template = testDataFactory.createRetroTemplate(
				project,
				"회고",
				"회고 템플릿",
				"Template Detail"
			);

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/retros/templates/{templateId}", url, template.getId())
						.cookie(new Cookie("accessToken", ownerAuth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 회고 템플릿 상세 조회 시도")
		public void getRetroTemplateDetailNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			testDataFactory.createRetroTemplate(
				project,
				"회고",
				"회고 템플릿",
				"Template Detail"
			);

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/retros/templates/{templateId}", url, 9999L)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.RETRO_TEMPLATE_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("delete retro template")
	class DeleteRetroTemplate {
		@BeforeEach
		void setUp() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - 회고 생성 템플릿 삭제 성공")
		public void deleteRetroTemplateSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			RetroTemplate template = testDataFactory.createRetroTemplate(
				project,
				"회고",
				"회고 템플릿",
				"Template Detail"
			);
			RetroTemplateDeleteReqDto deleteReqDto = new RetroTemplateDeleteReqDto(template.getId());
			String deleteJson = objectMapper.writeValueAsString(deleteReqDto);

			// when
			mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/retros/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(deleteJson))
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThatThrownBy(() -> testDataFactory.findRetroTemplateById(template.getId()))
				.isInstanceOf(Exception.class);
		}

		@Test
		@DisplayName("403 - 멤버가 아닌 사용자가 회고 생성 템플릿 삭제 시도")
		public void deleteRetroTemplateForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			RetroTemplate template = testDataFactory.createRetroTemplate(
				project,
				"회고",
				"회고 템플릿",
				"Template Detail"
			);
			RetroTemplateDeleteReqDto deleteReqDto = new RetroTemplateDeleteReqDto(template.getId());
			String deleteJson = objectMapper.writeValueAsString(deleteReqDto);

			// when
			String response = mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/retros/templates", url)
						.cookie(new Cookie("accessToken", ownerAuth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(deleteJson))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 회고 템플릿 삭제 시도")
		public void deleteRetroTemplateNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			testDataFactory.createRetroTemplate(
				project,
				"회고",
				"회고 템플릿",
				"Template Detail"
			);
			RetroTemplateDeleteReqDto deleteReqDto = new RetroTemplateDeleteReqDto(9999L);
			String deleteJson = objectMapper.writeValueAsString(deleteReqDto);

			// when
			String response = mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/retros/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(deleteJson))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.RETRO_TEMPLATE_NOT_FOUND.getMessage());
		}
	}
}

