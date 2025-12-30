package scrumpledpaper.agiler.note;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import scrumpledpaper.agiler.annotation.IntegrationTest;
import scrumpledpaper.agiler.common.AuthContext;
import scrumpledpaper.agiler.common.TestDataFactory;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.note.dto.internal.NoteContentsUpdateReqDto;
import scrumpledpaper.agiler.note.entity.Meeting;
import scrumpledpaper.agiler.note.entity.Retro;
import scrumpledpaper.agiler.note.entity.Scrum;
import scrumpledpaper.agiler.project.entity.Project;

@IntegrationTest
@Transactional
public class InternalNoteControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private TestDataFactory testDataFactory;

	Image defaultImage;

	@Nested
	@DisplayName("GET /api/v1/internal/{type}/{id} - Note 조회")
	class GetNote {
		@BeforeEach
		void setUp() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - Retro 노트 조회 성공")
		public void getRetroNoteSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			Project project = testDataFactory.createProjectAndOwnerProfile("test-url", auth.getUser());
			Retro retro = testDataFactory.createRetro(project, "회고 제목", "회고 내용입니다.");

			// when & then
			String response = mockMvc.perform(
					get("/api/v1/internal/{type}/{id}", "retro", retro.getId())
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			assertThat(response).contains("회고 제목");
			assertThat(response).contains("회고 내용입니다.");
			assertThat(response).contains("\"type\":\"retro\"");
		}

		@Test
		@DisplayName("200 - Scrum 노트 조회 성공")
		public void getScrumNoteSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			Project project = testDataFactory.createProjectAndOwnerProfile("test-url", auth.getUser());
			Scrum scrum = testDataFactory.createScrum(project, "스크럼 제목", "스크럼 내용입니다.");

			// when & then
			String response = mockMvc.perform(
					get("/api/v1/internal/{type}/{id}", "scrum", scrum.getId())
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			assertThat(response).contains("스크럼 제목");
			assertThat(response).contains("스크럼 내용입니다.");
			assertThat(response).contains("\"type\":\"scrum\"");
		}

		@Test
		@DisplayName("200 - Meeting 노트 조회 성공")
		public void getMeetingNoteSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			Project project = testDataFactory.createProjectAndOwnerProfile("test-url", auth.getUser());
			Meeting meeting = testDataFactory.createMeeting(project, "회의 제목", "회의 내용입니다.");

			// when & then
			String response = mockMvc.perform(
					get("/api/v1/internal/{type}/{id}", "meeting", meeting.getId())
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			assertThat(response).contains("회의 제목");
			assertThat(response).contains("회의 내용입니다.");
			assertThat(response).contains("\"type\":\"meeting\"");
		}

		@Test
		@DisplayName("404 - 존재하지 않는 Retro 노트 조회")
		public void getRetroNoteNotFound() throws Exception {
			// when & then
			String response = mockMvc.perform(
					get("/api/v1/internal/{type}/{id}", "retro", 9999L)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			assertThat(response).contains(ErrorCode.RETRO_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 Scrum 노트 조회")
		public void getScrumNoteNotFound() throws Exception {
			// when & then
			String response = mockMvc.perform(
					get("/api/v1/internal/{type}/{id}", "scrum", 9999L)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			assertThat(response).contains(ErrorCode.SCRUM_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 Meeting 노트 조회")
		public void getMeetingNoteNotFound() throws Exception {
			// when & then
			String response = mockMvc.perform(
					get("/api/v1/internal/{type}/{id}", "meeting", 9999L)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			assertThat(response).contains(ErrorCode.MEETING_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("400 - 유효하지 않은 노트 타입 조회")
		public void getNoteInvalidType() throws Exception {
			// when & then
			String response = mockMvc.perform(
					get("/api/v1/internal/{type}/{id}", "invalid", 1L)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getContentAsString();

			assertThat(response).contains(ErrorCode.INVALID_NOTE_TYPE.getMessage());
		}
	}

	@Nested
	@DisplayName("PUT /api/v1/internal/{type}/{id}/contents - Note contents 수정")
	class UpdateNoteContents {
		@BeforeEach
		void setUp() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - Retro 노트 contents 수정 성공")
		public void updateRetroContentsSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			Project project = testDataFactory.createProjectAndOwnerProfile("test-url", auth.getUser());
			Retro retro = testDataFactory.createRetro(project, "회고 제목", "원본 내용");
			NoteContentsUpdateReqDto updateReqDto = new NoteContentsUpdateReqDto("수정된 회고 내용입니다.");
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			mockMvc.perform(
					put("/api/v1/internal/{type}/{id}/contents", "retro", retro.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNoContent());

			// then
			Retro updatedRetro = testDataFactory.findRetroById(retro.getId());
			assertThat(updatedRetro.getContents()).isEqualTo("수정된 회고 내용입니다.");
		}

		@Test
		@DisplayName("204 - Scrum 노트 contents 수정 성공")
		public void updateScrumContentsSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			Project project = testDataFactory.createProjectAndOwnerProfile("test-url", auth.getUser());
			Scrum scrum = testDataFactory.createScrum(project, "스크럼 제목", "원본 내용");
			NoteContentsUpdateReqDto updateReqDto = new NoteContentsUpdateReqDto("수정된 스크럼 내용입니다.");
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			mockMvc.perform(
					put("/api/v1/internal/{type}/{id}/contents", "scrum", scrum.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNoContent());

			// then
			Scrum updatedScrum = testDataFactory.findScrumById(scrum.getId());
			assertThat(updatedScrum.getContents()).isEqualTo("수정된 스크럼 내용입니다.");
		}

		@Test
		@DisplayName("204 - Meeting 노트 contents 수정 성공")
		public void updateMeetingContentsSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			Project project = testDataFactory.createProjectAndOwnerProfile("test-url", auth.getUser());
			Meeting meeting = testDataFactory.createMeeting(project, "회의 제목", "원본 내용");
			NoteContentsUpdateReqDto updateReqDto = new NoteContentsUpdateReqDto("수정된 회의 내용입니다.");
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			mockMvc.perform(
					put("/api/v1/internal/{type}/{id}/contents", "meeting", meeting.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNoContent());

			// then
			Meeting updatedMeeting = testDataFactory.findMeetingById(meeting.getId());
			assertThat(updatedMeeting.getContents()).isEqualTo("수정된 회의 내용입니다.");
		}

		@Test
		@DisplayName("404 - 존재하지 않는 노트 contents 수정 시도")
		public void updateNoteContentsNotFound() throws Exception {
			// given
			NoteContentsUpdateReqDto updateReqDto = new NoteContentsUpdateReqDto("수정 내용");
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when & then
			String response = mockMvc.perform(
					put("/api/v1/internal/{type}/{id}/contents", "retro", 9999L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			assertThat(response).contains(ErrorCode.RETRO_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("400 - 유효하지 않은 노트 타입으로 contents 수정 시도")
		public void updateNoteContentsInvalidType() throws Exception {
			// given
			NoteContentsUpdateReqDto updateReqDto = new NoteContentsUpdateReqDto("수정 내용");
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when & then
			String response = mockMvc.perform(
					put("/api/v1/internal/{type}/{id}/contents", "invalid", 1L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getContentAsString();

			assertThat(response).contains(ErrorCode.INVALID_NOTE_TYPE.getMessage());
		}
	}

	@Nested
	@DisplayName("GET /api/v1/internal/permission - 권한 확인")
	class CheckPermission {
		@BeforeEach
		void setUp() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 프로젝트 멤버의 Retro 노트 권한 확인 (editable: true)")
		public void checkRetroPermissionMember() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			Project project = testDataFactory.createProjectAndOwnerProfile("test-url", auth.getUser());
			Retro retro = testDataFactory.createRetro(project, "회고 제목", "회고 내용");

			// when & then
			String response = mockMvc.perform(
					get("/api/v1/internal/permission")
						.param("type", "retro")
						.param("id", String.valueOf(retro.getId()))
						.param("userId", String.valueOf(auth.getUser().getId()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			assertThat(response).contains("\"editable\":true");
		}

		@Test
		@DisplayName("200 - 프로젝트 멤버의 Scrum 노트 권한 확인 (editable: true)")
		public void checkScrumPermissionMember() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			Project project = testDataFactory.createProjectAndOwnerProfile("test-url", auth.getUser());
			Scrum scrum = testDataFactory.createScrum(project, "스크럼 제목", "스크럼 내용");

			// when & then
			String response = mockMvc.perform(
					get("/api/v1/internal/permission")
						.param("type", "scrum")
						.param("id", String.valueOf(scrum.getId()))
						.param("userId", String.valueOf(auth.getUser().getId()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			assertThat(response).contains("\"editable\":true");
		}

		@Test
		@DisplayName("200 - 프로젝트 멤버의 Meeting 노트 권한 확인 (editable: true)")
		public void checkMeetingPermissionMember() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			Project project = testDataFactory.createProjectAndOwnerProfile("test-url", auth.getUser());
			Meeting meeting = testDataFactory.createMeeting(project, "회의 제목", "회의 내용");

			// when & then
			String response = mockMvc.perform(
					get("/api/v1/internal/permission")
						.param("type", "meeting")
						.param("id", String.valueOf(meeting.getId()))
						.param("userId", String.valueOf(auth.getUser().getId()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			assertThat(response).contains("\"editable\":true");
		}

		@Test
		@DisplayName("200 - 프로젝트 비멤버의 노트 권한 확인 (editable: false)")
		public void checkPermissionNonMember() throws Exception {
			// given
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			AuthContext nonMemberAuth = testDataFactory.createAuth(defaultImage);
			Project project = testDataFactory.createProjectAndOwnerProfile("test-url", ownerAuth.getUser());
			Retro retro = testDataFactory.createRetro(project, "회고 제목", "회고 내용");

			// when & then
			String response = mockMvc.perform(
					get("/api/v1/internal/permission")
						.param("type", "retro")
						.param("id", String.valueOf(retro.getId()))
						.param("userId", String.valueOf(nonMemberAuth.getUser().getId()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			assertThat(response).contains("\"editable\":false");
		}

		@Test
		@DisplayName("404 - 존재하지 않는 노트의 권한 확인")
		public void checkPermissionNoteNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);

			// when & then
			String response = mockMvc.perform(
					get("/api/v1/internal/permission")
						.param("type", "retro")
						.param("id", "9999")
						.param("userId", String.valueOf(auth.getUser().getId()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			assertThat(response).contains(ErrorCode.RETRO_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("400 - 유효하지 않은 노트 타입의 권한 확인")
		public void checkPermissionInvalidType() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);

			// when & then
			String response = mockMvc.perform(
					get("/api/v1/internal/permission")
						.param("type", "invalid")
						.param("id", "1")
						.param("userId", String.valueOf(auth.getUser().getId()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getContentAsString();

			assertThat(response).contains(ErrorCode.INVALID_NOTE_TYPE.getMessage());
		}
	}
}