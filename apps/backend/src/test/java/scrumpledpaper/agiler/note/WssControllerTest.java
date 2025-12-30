package scrumpledpaper.agiler.note;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import scrumpledpaper.agiler.annotation.IntegrationTest;
import scrumpledpaper.agiler.common.AuthContext;
import scrumpledpaper.agiler.common.TestDataFactory;
import scrumpledpaper.agiler.common.config.AppProperties;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.common.utils.WssTokenProvider;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.note.dto.NoteUpdateReqDto;
import scrumpledpaper.agiler.note.entity.Meeting;
import scrumpledpaper.agiler.note.entity.Retro;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;

@IntegrationTest
@Transactional
public class WssControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private TestDataFactory testDataFactory;
	@Autowired
	private WssTokenProvider wssTokenProvider;
	@Autowired
	private AppProperties appProperties;
	Image defaultImage;

	@Nested
	@DisplayName("Get Wss Token API")
	class GetWssTokenApi {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - Wss 토큰 발급 성공 - meeting")
		public void meetingWssTokenSuccess() throws Exception {
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile authProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
			Meeting meeting = testDataFactory.createMeetingWithParticipants(project, List.of(authProfile));
			String docId = "meeting-" + meeting.getId();

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/{docId}", url, docId)
						.cookie(new Cookie("accessToken", auth.getToken())))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).isNotBlank();

			String token = objectMapper.readTree(response).get("wssToken").asText();
			Claims claims = wssTokenProvider.getClaims(token);

			assertThat(claims.getSubject()).isEqualTo(auth.getUser().getId().toString());
			assertThat(claims.get("docId", String.class)).isEqualTo(docId);
			assertThat(claims.getIssuedAt()).isNotNull();
			assertThat(claims.getExpiration()).isNotNull();
		}

		@Test
		@DisplayName("200 - Wss 토큰 발급 성공 - retro")
		public void retroWssTokenSuccess() throws Exception {
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile authProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
			Retro retro = testDataFactory.createRetroWithParticipants(project, List.of(authProfile));
			String docId = "retro-" + retro.getId();

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/{docId}", url, docId)
						.cookie(new Cookie("accessToken", auth.getToken())))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).isNotBlank();

			String token = objectMapper.readTree(response).get("wssToken").asText();
			Claims claims = wssTokenProvider.getClaims(token);

			assertThat(claims.getSubject()).isEqualTo(auth.getUser().getId().toString());
			assertThat(claims.get("docId", String.class)).isEqualTo(docId);
			assertThat(claims.getIssuedAt()).isNotNull();
			assertThat(claims.getExpiration()).isNotNull();
		}

		@Test
		@DisplayName("200 - Wss 토큰 발급 성공 - scrum")
		public void scrumWssTokenSuccess() throws Exception {
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile authProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(),
				project.getId());
			String docId =
				"scrum-" + testDataFactory.createScrumWithParticipants(project, List.of(authProfile)).getId();

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/{docId}", url, docId)
						.cookie(new Cookie("accessToken", auth.getToken())))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).isNotBlank();

			String token = objectMapper.readTree(response).get("wssToken").asText();
			Claims claims = wssTokenProvider.getClaims(token);

			assertThat(claims.getSubject()).isEqualTo(auth.getUser().getId().toString());
			assertThat(claims.get("docId", String.class)).isEqualTo(docId);
			assertThat(claims.getIssuedAt()).isNotNull();
			assertThat(claims.getExpiration()).isNotNull();
		}

		@ParameterizedTest
		@DisplayName("404 - Wss 토큰 발급 실패 - 존재하지 않는 문서")
		@ValueSource(strings = { "meeting-9999", "retro-9999", "scrum-9999" })
		public void wssTokenFail_NotFoundDocument(String docId) throws Exception {
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/{docId}", url, docId)
						.cookie(new Cookie("accessToken", auth.getToken())))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.NOTE_NOT_FOUND.getMessage());
		}

		@ParameterizedTest
		@DisplayName("400 - Wss 토큰 발급 실패 - 잘못된 문서 ID 포맷")
		@ValueSource(strings = { "meeting-", "retro-abc", "scrum-12ab", "unknown-123", "invalidformat", "1-2-3" })
		public void wssTokenFail_InvalidDocumentIdFormat(String docId) throws Exception {
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/{docId}", url, docId)
						.cookie(new Cookie("accessToken", auth.getToken())))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.INVALID_DOCUMENT_ID.getMessage());
		}

		@Test
		@DisplayName("403 - Wss 토큰 발급 실패 - 문서에 대한 접근 권한 없음")
		public void wssTokenFail_NoAccessToDocument() throws Exception {
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext otherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Meeting meeting = testDataFactory.createMeetingWithParticipants(project, List.of());
			String docId = "meeting-" + meeting.getId();

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/{docId}", url, docId)
						.cookie(new Cookie("accessToken", otherAuth.getToken())))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}
	}

	@Nested
	@DisplayName("Get Retro Detail API")
	class GetRetroDetailApi {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 회고 상세 조회 성공")
		public void getRetroDetailSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile authProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
			Retro retro = testDataFactory.createRetroWithParticipants(project, List.of(authProfile));
			String apiKey = appProperties.getApi().getKey();

			// when
			String response = mockMvc.perform(
					get("/internal/api/v1/docs/retro/{id}", retro.getId())
						.header("X-API-KEY", apiKey))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).isNotBlank();
			assertThat(response).contains(retro.getId().toString());
			assertThat(response).contains(retro.getTitle());
			assertThat(response).contains(retro.getContents());
			assertThat(response).contains(authProfile.getId().toString());
			assertThat(response).contains(authProfile.getNickname());
		}

		@Test
		@DisplayName("404 - 회고 상세 조회 실패 - 존재하지 않는 회고")
		public void getRetroDetailFail_NotFoundRetro() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			String apiKey = appProperties.getApi().getKey();

			// when
			String response = mockMvc.perform(
					get("/internal/api/v1/docs/retro/{id}", 9999L)
						.header("X-API-KEY", apiKey))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.NOTE_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("Get Meeting Detail API")
	class GetMeetingDetailApi {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 회의 상세 조회 성공")
		public void getMeetingDetailSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile authProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(),
				project.getId());
			Meeting meeting = testDataFactory.createMeetingWithParticipants(project, List.of(authProfile));
			String apiKey = appProperties.getApi().getKey();

			// when
			String response = mockMvc.perform(
					get("/internal/api/v1/docs/meeting/{id}", meeting.getId())
						.header("X-API-KEY", apiKey))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).isNotBlank();
			assertThat(response).contains(meeting.getId().toString());
			assertThat(response).contains(meeting.getTitle());
			assertThat(response).contains(meeting.getContents());
			assertThat(response).contains(authProfile.getId().toString());
			assertThat(response).contains(authProfile.getNickname());
		}

		@Test
		@DisplayName("404 - 회의 상세 조회 실패 - 존재하지 않는 회의")
		public void getMeetingDetailFail_NotFoundMeeting() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			String apiKey = appProperties.getApi().getKey();

			// when
			String response = mockMvc.perform(
					get("/internal/api/v1/docs/meeting/{id}", 9999L)
						.header("X-API-KEY", apiKey))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.NOTE_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("Update Meeting API")
	class UpdateMeetingApi{
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 회의 상세 조회 성공")
		public void getMeetingDetailSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile authProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(),
				project.getId());
			Meeting meeting = testDataFactory.createMeetingWithParticipants(project, List.of(authProfile));
			String apiKey = appProperties.getApi().getKey();
			NoteUpdateReqDto updateReqDto = new NoteUpdateReqDto("Updated Title", "Updated Contents");

			// when
			String response = mockMvc.perform(
					put("/internal/api/v1/docs/meeting/{id}", meeting.getId())
						.header("X-API-KEY", apiKey)
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(updateReqDto)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			Meeting updatedMeeting = testDataFactory.findMeetingById(meeting.getId());
			assertThat(updatedMeeting.getTitle()).isEqualTo("Updated Title");
			assertThat(updatedMeeting.getContents()).isEqualTo("Updated Contents");
		}

		@Test
		@DisplayName("404 - 회의 상세 조회 실패 - 존재하지 않는 회의")
		public void getMeetingDetailFail_NotFoundMeeting() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			String apiKey = appProperties.getApi().getKey();
			NoteUpdateReqDto updateReqDto = new NoteUpdateReqDto("Updated Title", "Updated Contents");

			// when
			String response = mockMvc.perform(
					put("/internal/api/v1/docs/meeting/{id}", 9999L)
						.header("X-API-KEY", apiKey)
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(updateReqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.NOTE_NOT_FOUND.getMessage());
		}
	}
}
