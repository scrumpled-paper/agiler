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
import scrumpledpaper.agiler.note.dto.NoteCreateReqDto;
import scrumpledpaper.agiler.note.dto.NoteDeleteReqDto;
import scrumpledpaper.agiler.note.dto.ScrumResDto;
import scrumpledpaper.agiler.note.entity.Scrum;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.entity.Role;
import scrumpledpaper.agiler.template.entity.ScrumTemplate;

@IntegrationTest
@Transactional
public class ScrumControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private TestDataFactory testDataFactory;
	Image defaultImage;

	@Nested
	@DisplayName("GET Scrums List Test")
	class GetScrumListTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - Scrum 리스트 조회 성공")
		public void scrumListSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext otherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile authProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
			Profile otherProfile = testDataFactory.createProfile(otherAuth.getUser(), project, Role.MEMBER);
			for (int i = 1; i <= 15; i++) {
				testDataFactory.createScrumWithParticipants(project, List.of(authProfile, otherProfile));
			}
			int page = 0;
			int size = 10;

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/scrums", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.param("page", page + "")
						.param("size", size + ""))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<ScrumResDto> resDto = objectMapper.readValue(response,
				new TypeReference<PageResDto<ScrumResDto>>() {});

			assertThat(resDto.getPageSize()).isEqualTo(10);
			assertThat(resDto.getCurrentPage()).isEqualTo(0);
			assertThat(resDto.getTotalPages()).isEqualTo(2);
			assertThat(resDto.getTotalElements()).isEqualTo(15);
			assertThat(resDto.getContents()).hasSize(10);

			Page<Scrum> meetingPage = testDataFactory.findScrumsByProjectIdPaged(project.getId(), page, size);
			for (int i = 0; i < meetingPage.getContent().size(); i++) {
				Scrum scrum = meetingPage.getContent().get(i);
				ScrumResDto meetingResDto = resDto.getContents().get(i);

				assertThat(meetingResDto.scrumId()).isEqualTo(scrum.getId());
				assertThat(meetingResDto.title()).isEqualTo(scrum.getTitle());
				assertThat(meetingResDto.createdAt()).isEqualTo(scrum.getCreatedAt());
				assertThat(meetingResDto.participants()).hasSize(2);
			}
		}

		@Test
		@DisplayName("200 - Scrum 빈 리스트 조회 성공")
		public void scrumEmptyListSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			int page = 0;
			int size = 10;

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/scrums", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.param("page", page + "")
						.param("size", size + ""))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<ScrumResDto> resDto = objectMapper.readValue(response,
				new TypeReference<PageResDto<ScrumResDto>>() {});

			assertThat(resDto.getPageSize()).isEqualTo(10);
			assertThat(resDto.getCurrentPage()).isEqualTo(0);
			assertThat(resDto.getTotalPages()).isEqualTo(0);
			assertThat(resDto.getTotalElements()).isEqualTo(0);
			assertThat(resDto.getContents()).isEmpty();
		}

		@Test
		@DisplayName("403 - 프로젝트 참여자가 아닌 사용자가 Scrum 리스트 조회 시도")
		public void scrumListForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext otherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			int page = 0;
			int size = 10;

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/scrums", url)
						.cookie(new Cookie("accessToken", otherAuth.getToken()))
						.param("page", page + "")
						.param("size", size + ""))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트의 Scrum 리스트 조회 시도")
		public void scrumListProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non-existent-url";
			int page = 0;
			int size = 10;

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/scrums", url)
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
	@DisplayName("Create Scrum Test")
	class CreateScrumTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - Scrum 생성 성공")
		public void createScrumSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url,  auth.getUser());
			ScrumTemplate template = testDataFactory.createScrumTemplate(project);
			NoteCreateReqDto reqDto = new NoteCreateReqDto(template.getId());

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/scrums", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			Scrum scrum = testDataFactory.findLatestScrumByProjectId(project.getId());
			assertThat(response).contains(scrum.getId().toString());

			assertThat(scrum.getTitle()).isEqualTo(template.getTitle());
			assertThat(scrum.getContents()).isEqualTo(template.getContents());
			assertThat(scrum.getProject().getId()).isEqualTo(project.getId());
		}

		@Test
		@DisplayName("200 - Scrum 템플릿 없이 생성 성공")
		public void createScrumWithoutTemplateSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url,  auth.getUser());
			NoteCreateReqDto reqDto = new NoteCreateReqDto(null);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/scrums", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			Scrum scrum = testDataFactory.findLatestScrumByProjectId(project.getId());
			assertThat(response).contains(scrum.getId().toString());

			assertThat(scrum.getTitle()).isEqualTo("");
			assertThat(scrum.getContents()).isEqualTo("");
			assertThat(scrum.getProject().getId()).isEqualTo(project.getId());
		}

		@Test
		@DisplayName("403 - 프로젝트 참여자가 아닌 사용자가 Scrum 생성 시도")
		public void createScrumForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext otherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			ScrumTemplate template = testDataFactory.createScrumTemplate(project);
			NoteCreateReqDto reqDto = new NoteCreateReqDto(template.getId());

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/scrums", url)
						.cookie(new Cookie("accessToken", otherAuth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 Scrum 생성 시도")
		public void createScrumProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non-existent-url";
			NoteCreateReqDto reqDto = new NoteCreateReqDto(null);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/scrums", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 Scrum 템플릿으로 Scrum 생성 시도")
		public void createScrumTemplateNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url,  auth.getUser());
			NoteCreateReqDto reqDto = new NoteCreateReqDto(9999L);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/scrums", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.SCRUM_TEMPLATE_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("Delete Scrum Test")
	class DeleteScrumTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - Scrum 삭제 성공")
		public void deleteScrumSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile authProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(), project.getId());
			Scrum scrum = testDataFactory.createScrumWithParticipants(project, List.of(authProfile));
			NoteDeleteReqDto reqDto = new NoteDeleteReqDto(scrum.getId());

			// when
			mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/scrums", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isNoContent());

			// then
			assertThat(testDataFactory.findScrumById(scrum.getId())).isNull();
		}

		@Test
		@DisplayName("403 - 프로젝트 참여자가 아닌 사용자가 Scrum 삭제 시도")
		public void deleteScrumForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext otherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile authProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(),
				project.getId());
			Scrum scrum = testDataFactory.createScrumWithParticipants(project, List.of(authProfile));
			NoteDeleteReqDto reqDto = new NoteDeleteReqDto(scrum.getId());

			// when
			String response = mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/scrums", url)
						.cookie(new Cookie("accessToken", otherAuth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
			assertThat(testDataFactory.findScrumById(scrum.getId())).isNotNull();
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트의 Scrum 삭제 시도")
		public void deleteScrumProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non-existent-url";
			NoteDeleteReqDto reqDto = new NoteDeleteReqDto(1L);

			// when
			String response = mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/scrums", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 Scrum 삭제 시도")
		public void deleteScrumNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			NoteDeleteReqDto reqDto = new NoteDeleteReqDto(9999L);

			// when
			String response = mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/scrums", url)
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
