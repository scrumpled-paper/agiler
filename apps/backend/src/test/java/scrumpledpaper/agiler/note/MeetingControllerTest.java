package scrumpledpaper.agiler.note;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import scrumpledpaper.agiler.annotation.IntegrationTest;
import scrumpledpaper.agiler.common.AuthContext;
import scrumpledpaper.agiler.common.PageResDto;
import scrumpledpaper.agiler.common.TestDataFactory;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.note.dto.MeetingResDto;
import scrumpledpaper.agiler.note.dto.NoteCreateReqDto;
import scrumpledpaper.agiler.note.dto.NoteDeleteReqDto;
import scrumpledpaper.agiler.note.entity.Meeting;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.entity.Role;
import scrumpledpaper.agiler.template.entity.MeetingTemplate;

@IntegrationTest
@Transactional
public class MeetingControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private TestDataFactory testDataFactory;
	Image defaultImage;

	@Nested
	@DisplayName("Get Meetings List Test")
	class GetMeetingListTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - Meeting 리스트 조회 성공")
		public void meetingListSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext otherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile authProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
			Profile otherProfile = testDataFactory.createProfile(otherAuth.getUser(), project, Role.MEMBER);
			for (int i = 1; i <= 15; i++) {
				testDataFactory.createMeetingWithParticipants(project, List.of(authProfile, otherProfile));
			}
			int page = 0;
			int size = 10;

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/meetings", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.param("page", page + "")
						.param("size", size + ""))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<MeetingResDto> resDto = objectMapper.readValue(response,
				new TypeReference<PageResDto<MeetingResDto>>() {});

			assertThat(resDto.getPageSize()).isEqualTo(10);
			assertThat(resDto.getCurrentPage()).isEqualTo(0);
			assertThat(resDto.getTotalPages()).isEqualTo(2);
			assertThat(resDto.getTotalElements()).isEqualTo(15);
			assertThat(resDto.getContents()).hasSize(10);

			Page<Meeting> meetingPage = testDataFactory.findMeetingsByProjectIdPaged(project.getId(), page, size);
			for (int i = 0; i < meetingPage.getContent().size(); i++) {
				Meeting meeting = meetingPage.getContent().get(i);
				var meetingResDto = resDto.getContents().get(i);

				assertThat(meetingResDto.meetingId()).isEqualTo(meeting.getId());
				assertThat(meetingResDto.title()).isEqualTo(meeting.getTitle());
				assertThat(meetingResDto.createdAt()).isEqualTo(meeting.getCreatedAt());
				assertThat(meetingResDto.participants()).hasSize(2);
			}
		}

		@Test
		@DisplayName("200 - Meeting 리스트 조회 - 회의가 없는 경우")
		public void meetingListEmptySuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			int page = 0;
			int size = 10;

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/meetings", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.param("page", page + "")
						.param("size", size + ""))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<MeetingResDto> resDto = objectMapper.readValue(response,
				new TypeReference<PageResDto<MeetingResDto>>() {});

			assertThat(resDto.getPageSize()).isEqualTo(10);
			assertThat(resDto.getCurrentPage()).isEqualTo(0);
			assertThat(resDto.getTotalPages()).isEqualTo(0);
			assertThat(resDto.getTotalElements()).isEqualTo(0);
			assertThat(resDto.getContents()).isEmpty();
		}

		@Test
		@DisplayName("403 - 프로젝트 참여자가 아닌 사용자가 Meeting 리스트 조회 시도")
		public void meetingListNotMember() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext otherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			int page = 0;
			int size = 10;

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/meetings", url)
						.cookie(new Cookie("accessToken", otherAuth.getToken()))
						.param("page", page + "")
						.param("size", size + ""))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트의 Meeting 리스트 조회 시도")
		public void meetingListProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non-existent-url";
			int page = 0;
			int size = 10;

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/meetings", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.param("page", page + "")
						.param("size", size + ""))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("Create Meeting Test")
	class CreateMeetingTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - Meeting 생성 성공")
		public void createMeetingSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			MeetingTemplate template = testDataFactory.createMeetingTemplate(project);
			NoteCreateReqDto reqDto = new NoteCreateReqDto(template.getId());

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/meetings", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			Meeting meeting = testDataFactory.findByLatestMeetingByProjectId(project.getId());
			assertThat(response).contains(meeting.getId().toString());

			assertThat(meeting.getTitle()).isEqualTo(template.getTitle());
			assertThat(meeting.getContents()).isEqualTo(template.getContents());
		}

		@Test
		@DisplayName("200 - 템플릿 없이 Meeting 생성 성공")
		public void createMeetingWithoutTemplateSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			NoteCreateReqDto reqDto = new NoteCreateReqDto(null);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/meetings", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			Meeting meeting = testDataFactory.findByLatestMeetingByProjectId(project.getId());
			assertThat(response).contains(meeting.getId().toString());

			assertThat(meeting.getTitle()).isEqualTo("");
			assertThat(meeting.getContents()).isEqualTo("");
		}

		@Test
		@DisplayName("403 - 프로젝트 참여자가 아닌 사용자가 Meeting 생성 시도")
		public void createMeetingNotMember() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext otherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			MeetingTemplate template = testDataFactory.createMeetingTemplate(project);
			NoteCreateReqDto reqDto = new NoteCreateReqDto(template.getId());

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/meetings", url)
						.cookie(new Cookie("accessToken", otherAuth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트의 Meeting 생성 시도")
		public void createMeetingProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non-existent-url";
			NoteCreateReqDto reqDto = new NoteCreateReqDto(1L);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/meetings", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 템플릿으로 Meeting 생성 시도")
		public void createMeetingTemplateNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			NoteCreateReqDto reqDto = new NoteCreateReqDto(999L);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/meetings", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.MEETING_TEMPLATE_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("Delete Meeting Test")
	class DeleteMeetingTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - Meeting 삭제 성공")
		public void deleteMeetingSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Meeting meeting = testDataFactory.createMeeting(project);
			NoteDeleteReqDto reqDto = new NoteDeleteReqDto(meeting.getId());

			// when
			String response = mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/meetings", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getContentAsString();


			// then
			assertThat(testDataFactory.findMeetingById(meeting.getId())).isNull();
		}

		@Test
		@DisplayName("403 - 프로젝트 참여자가 아닌 사용자가 Meeting 삭제 시도")
		public void deleteMeetingNotMember() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext otherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Meeting meeting = testDataFactory.createMeeting(project);
			NoteDeleteReqDto reqDto = new NoteDeleteReqDto(meeting.getId());

			// when
			String response = mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/meetings", url)
						.cookie(new Cookie("accessToken", otherAuth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트의 Meeting 삭제 시도")
		public void deleteMeetingProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non-existent-url";
			NoteDeleteReqDto reqDto = new NoteDeleteReqDto(1L);

			// when
			String response = mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/meetings", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 Meeting 삭제 시도")
		public void deleteMeetingNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			NoteDeleteReqDto reqDto = new NoteDeleteReqDto(999L);

			// when
			String response = mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/meetings", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.NOTE_NOT_FOUND.getMessage());
		}
	}
}
