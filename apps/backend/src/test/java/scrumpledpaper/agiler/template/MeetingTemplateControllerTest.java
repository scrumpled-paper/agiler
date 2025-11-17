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
import scrumpledpaper.agiler.template.dto.MeetingTemplateCreateReqDto;
import scrumpledpaper.agiler.template.dto.MeetingTemplateUpdateReqDto;
import scrumpledpaper.agiler.template.entity.MeetingTemplate;

@IntegrationTest
@Transactional
public class MeetingTemplateControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private TestDataFactory testDataFactory;
	Image defaultImage;

	@Nested
	@DisplayName("Create Meeting Template")
	class CreateMeetingTemplate {
		@BeforeEach
		void setUp() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - 회의 생성 템플릿 생성 성공")
		public void meetingTemplateCreateSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			MeetingTemplateCreateReqDto createReqDto = new MeetingTemplateCreateReqDto(
				"회의 템플릿 제목",
				"회의 템플릿 설명",
				"회의 템플릿 내용"
			);
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/meetings/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getContentAsString();

			// then
			List<MeetingTemplate> meetingTemplates = testDataFactory.findMeetingTemplatesByProjectId(project.getId());
			assertThat(meetingTemplates)
				.anyMatch(meetingTemplate ->
					meetingTemplate.getTitle().equals(createReqDto.title()) &&
					meetingTemplate.getDescription().equals(createReqDto.description()) &&
					meetingTemplate.getContents().equals(createReqDto.contents())
				);
		}

		@Test
		@DisplayName("403 - 멤버가 아닌 사용자가 회의 생성 템플릿 생성 시도")
		public void meetingTemplateCreateForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());
			MeetingTemplateCreateReqDto createReqDto = new MeetingTemplateCreateReqDto(
				"회의 템플릿 제목",
				"회의 템플릿 설명",
				"회의 템플릿 내용"
			);
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/meetings/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 회의 템플릿 생성 시도")
		public void meetingTemplateCreateProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non_existing_url";
			MeetingTemplateCreateReqDto createReqDto = new MeetingTemplateCreateReqDto(
				"회의 템플릿 제목",
				"회의 템플릿 설명",
				"회의 템플릿 내용"
			);
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/meetings/templates", url)
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
	@DisplayName("update meeting template")
	class UpdateMeetingTemplate {
		private MeetingTemplate existingTemplate;

		@BeforeEach
		void setUp() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - 회의 생성 템플릿 수정 성공")
		public void meetingTemplateUpdateSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			existingTemplate = testDataFactory.createMeetingTemplate(
				project,
				"회의",
				"회의 템플릿",
				"Old Template"
			);
			MeetingTemplateUpdateReqDto updateReqDto = new MeetingTemplateUpdateReqDto(
				existingTemplate.getId(),
				"update template",
				"update meeting template",
				"Updated Template"
			);
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/meetings/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getContentAsString();

			// then
			MeetingTemplate updatedTemplate = testDataFactory.findMeetingTemplateById(existingTemplate.getId());
			assertThat(updatedTemplate.getTitle()).isEqualTo(updateReqDto.title());
			assertThat(updatedTemplate.getDescription()).isEqualTo(updateReqDto.description());
			assertThat(updatedTemplate.getContents()).isEqualTo(updateReqDto.contents());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 회의 템플릿 수정 시도")
		public void meetingTemplateUpdateTemplateNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			MeetingTemplateUpdateReqDto updateReqDto = new MeetingTemplateUpdateReqDto(
				9999L,
				"update template",
				"update meeting template",
				"Updated Template"
			);
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/meetings/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.MEETING_TEMPLATE_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("403 - 멤버가 아닌 사용자가 회의 생성 템플릿 수정 시도")
		public void meetingTemplateUpdateForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			existingTemplate = testDataFactory.createMeetingTemplate(
				project,
				"회의",
				"회의 템플릿",
				"Old Template"
			);
			MeetingTemplateUpdateReqDto updateReqDto = new MeetingTemplateUpdateReqDto(
				9999L,
				"update template",
				"update meeting template",
				"Updated Template"
			);
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/meetings/templates", url)
						.cookie(new Cookie("accessToken", ownerAuth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 회의 템플릿 수정 시도")
		public void meetingTemplateUpdateProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non_existing_url";
			MeetingTemplateUpdateReqDto updateReqDto = new MeetingTemplateUpdateReqDto(
				9999L,
				"update template",
				"update meeting template",
				"Updated Template"
			);
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/meetings/templates", url)
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
	@DisplayName("get meeting template list")
	class GetMeetingTemplateList {
		@BeforeEach
		void setUp() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 회의 생성 템플릿 리스트 조회 성공")
		public void getMeetingTemplateListSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			MeetingTemplate template1 = testDataFactory.createMeetingTemplate(
				project,
				"회의",
				"회의 템플릿",
				"Template 1"
			);
			MeetingTemplate template2 = testDataFactory.createMeetingTemplate(
				project,
				"스프린트",
				"스프린트 템플릿",
				"Template 2"
			);

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/meetings/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(template1.getTitle());
			assertThat(response).contains(template2.getTitle());
		}

		@Test
		@DisplayName("403 - 멤버가 아닌 사용자가 회의 생성 템플릿 리스트 조회 시도")
		public void getMeetingTemplateListForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/meetings/templates", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}
	}

	@Nested
	@DisplayName("get meeting template detail")
	class GetMeetingTemplateDetail {
		@BeforeEach
		void setUp() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 회의 생성 템플릿 상세 조회 성공")
		public void getMeetingTemplateDetailSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			MeetingTemplate template = testDataFactory.createMeetingTemplate(
				project,
				"회의",
				"회의 템플릿",
				"Template Detail"
			);

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/meetings/templates/{templateId}", url, template.getId())
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(template.getTitle());
			assertThat(response).contains(template.getContents());
		}

		@Test
		@DisplayName("403 - 멤버가 아닌 사용자가 회의 생성 템플릿 상세 조회 시도")
		public void getMeetingTemplateDetailForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url,	auth.getUser());
			MeetingTemplate template = testDataFactory.createMeetingTemplate(
				project,
				"회의",
				"회의 템플릿",
				"Template Detail"
			);

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/meetings/templates/{templateId}", url, template.getId())
						.cookie(new Cookie("accessToken", ownerAuth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 회의 템플릿 상세 조회 시도")
		public void getMeetingTemplateDetailNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			testDataFactory.createMeetingTemplate(
				project,
				"회의",
				"회의 템플릿",
				"Template Detail"
			);

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/meetings/templates/{templateId}", url, 9999L)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.MEETING_TEMPLATE_NOT_FOUND.getMessage());
		}
	}

}

