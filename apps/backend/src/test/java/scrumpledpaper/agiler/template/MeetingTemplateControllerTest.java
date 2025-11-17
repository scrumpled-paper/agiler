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

}

