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
import scrumpledpaper.agiler.note.dto.NoteParticipantResDto;
import scrumpledpaper.agiler.note.dto.NoteParticipantUpdateReqDto;
import scrumpledpaper.agiler.note.dto.RetroResDto;
import scrumpledpaper.agiler.note.entity.Retro;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.entity.Role;
import scrumpledpaper.agiler.template.entity.RetroTemplate;

@IntegrationTest
@Transactional
public class RetroControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private TestDataFactory testDataFactory;
	Image defaultImage;

	@Nested
	@DisplayName("Get Retro List Test")
	class GetRetroListTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - Retro 리스트 조회 성공")
		public void retroListSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext otherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile authProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(),
				project.getId());
			Profile otherProfile = testDataFactory.createProfile(otherAuth.getUser(), project, Role.MEMBER);
			for (int i = 1; i <= 15; i++) {
				testDataFactory.createRetroWithParticipants(project, List.of(authProfile, otherProfile));
			}
			int page = 0;
			int size = 10;

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/retros", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.param("page", page + "")
						.param("size", size + ""))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<RetroResDto> resDto = objectMapper.readValue(response,
				new TypeReference<PageResDto<RetroResDto>>() {
				});

			assertThat(resDto.getPageSize()).isEqualTo(10);
			assertThat(resDto.getCurrentPage()).isEqualTo(0);
			assertThat(resDto.getTotalPages()).isEqualTo(2);
			assertThat(resDto.getTotalElements()).isEqualTo(15);
			assertThat(resDto.getContents()).hasSize(10);

			Page<Retro> retroPage = testDataFactory.findRetrosByProjectIdPaged(project.getId(), page, size);
			for (int i = 0; i < retroPage.getContent().size(); i++) {
				Retro retro = retroPage.getContent().get(i);
				RetroResDto retroResDto = resDto.getContents().get(i);

				assertThat(retroResDto.retroId()).isEqualTo(retro.getId());
				assertThat(retroResDto.title()).isEqualTo(retro.getTitle());
				assertThat(retroResDto.createdAt()).isEqualTo(retro.getCreatedAt());
				assertThat(retroResDto.participants()).hasSize(2);
			}
		}

		@Test
		@DisplayName("200 - Retro 리스트 조회 성공 (빈 리스트)")
		public void retroListEmptySuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			int page = 0;
			int size = 10;

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/retros", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.param("page", page + "")
						.param("size", size + ""))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<RetroResDto> resDto = objectMapper.readValue(response,
				new TypeReference<PageResDto<RetroResDto>>() {
				});

			assertThat(resDto.getPageSize()).isEqualTo(10);
			assertThat(resDto.getCurrentPage()).isEqualTo(0);
			assertThat(resDto.getTotalPages()).isEqualTo(0);
			assertThat(resDto.getTotalElements()).isEqualTo(0);
			assertThat(resDto.getContents()).isEmpty();
		}

		@Test
		@DisplayName("403 - 프로젝트 참여자가 아닌 사용자의 Retro 리스트 조회 실패")
		public void retroListForbiddenFail() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext otherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			int page = 0;
			int size = 10;

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/retros", url)
						.cookie(new Cookie("accessToken", otherAuth.getToken()))
						.param("page", page + "")
						.param("size", size + ""))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트의 Retro 리스트 조회 실패")
		public void retroListNotFoundFail() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non-existent-url";
			int page = 0;
			int size = 10;

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/retros", url)
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
	@DisplayName("Create Retro Test")
	class CreateRetroTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - Retro 생성 성공")
		public void createRetroSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			RetroTemplate template = testDataFactory.createRetroTemplate(project);
			NoteCreateReqDto reqDto = new NoteCreateReqDto(template.getId());

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/retros", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			Retro retro = testDataFactory.findByLatestRetroByProjectId(project.getId());
			assertThat(response).contains(retro.getId().toString());

			assertThat(retro.getTitle()).isEqualTo(template.getTitle());
			assertThat(retro.getContents()).isEqualTo(template.getContents());
		}

		@Test
		@DisplayName("200 - Retro 생성 성공 (템플릿 미사용)")
		public void createRetroWithoutTemplateSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			NoteCreateReqDto reqDto = new NoteCreateReqDto(null);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/retros", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			Retro retro = testDataFactory.findByLatestRetroByProjectId(project.getId());
			assertThat(response).contains(retro.getId().toString());

			assertThat(retro.getTitle()).isEqualTo("");
			assertThat(retro.getContents()).isEqualTo("");
		}

		@Test
		@DisplayName("403 - 프로젝트 참여자가 아닌 사용자의 Retro 생성 실패")
		public void createRetroForbiddenFail() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext otherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			testDataFactory.createRetroTemplate(project);
			NoteCreateReqDto reqDto = new NoteCreateReqDto(null);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/retros", url)
						.cookie(new Cookie("accessToken", otherAuth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트의 Retro 생성 실패")
		public void createRetroNotFoundFail() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non-existent-url";
			NoteCreateReqDto reqDto = new NoteCreateReqDto(null);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/retros", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 템플릿으로 Retro 생성 실패")
		public void createRetroTemplateNotFoundFail() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Long nonExistentTemplateId = 9999L;
			NoteCreateReqDto reqDto = new NoteCreateReqDto(nonExistentTemplateId);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/retros", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.RETRO_TEMPLATE_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("Delete Retro Test")
	class DeleteRetroTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - Retro 삭제 성공")
		public void deleteRetroSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Retro retro = testDataFactory.createRetro(project);
			NoteDeleteReqDto reqDto = new NoteDeleteReqDto(retro.getId());

			// when
			String response = mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/retros", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(testDataFactory.findRetroById(retro.getId())).isNull();
		}

		@Test
		@DisplayName("403 - 프로젝트 참여자가 아닌 사용자의 Retro 삭제 실패")
		public void deleteRetroForbiddenFail() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext otherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Retro retro = testDataFactory.createRetro(project);
			NoteDeleteReqDto reqDto = new NoteDeleteReqDto(retro.getId());

			// when
			String response = mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/retros", url)
						.cookie(new Cookie("accessToken", otherAuth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
			assertThat(testDataFactory.findRetroById(retro.getId())).isNotNull();
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트의 Retro 삭제 실패")
		public void deleteRetroNotFoundFail() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non-existent-url";
			NoteDeleteReqDto reqDto = new NoteDeleteReqDto(1L);

			// when
			String response = mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/retros", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 Retro 삭제 실패")
		public void deleteRetroNotFoundFail2() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Long nonExistentRetroId = 9999L;
			NoteDeleteReqDto reqDto = new NoteDeleteReqDto(nonExistentRetroId);

			// when
			String response = mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/retros", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.NOTE_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("Update Retro Participants Test")
	class UpdateRetroParticipantsTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - Retro 참가자 업데이트 성공")
		public void updateRetroParticipantsSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext otherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile authProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(),
				project.getId());
			Profile otherProfile = testDataFactory.createProfile(otherAuth.getUser(), project, Role.MEMBER);
			Retro retro = testDataFactory.createRetroWithParticipants(project, List.of(authProfile));
			NoteParticipantUpdateReqDto reqDto = new NoteParticipantUpdateReqDto(
				List.of(otherProfile.getId())
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/retros/{retroId}", url, retro.getId())
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			NoteParticipantResDto resDto = objectMapper.readValue(response, NoteParticipantResDto.class);
			assertThat(resDto.participants()).hasSize(1);
			assertThat(resDto.participants().get(0).profileId()).isEqualTo(otherProfile.getId());
			assertThat(resDto.participants().get(0).nickname()).isEqualTo(otherProfile.getNickname());
		}

		@Test
		@DisplayName("200 - Retro 참가자 업데이트 성공 (빈 참가자 리스트)")
		public void updateRetroParticipantsEmptySuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile authProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(),
				project.getId());
			Retro retro = testDataFactory.createRetroWithParticipants(project, List.of(authProfile));
			NoteParticipantUpdateReqDto reqDto = new NoteParticipantUpdateReqDto(
				List.of()
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/retros/{retroId}", url, retro.getId())
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			NoteParticipantResDto resDto = objectMapper.readValue(response, NoteParticipantResDto.class);
			assertThat(resDto.participants()).isEmpty();
		}

		@Test
		@DisplayName("403 - 프로젝트 참여자가 아닌 사용자의 Retro 참가자 업데이트 실패")
		public void updateRetroParticipantsForbiddenFail() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext otherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile authProfile = testDataFactory.findProfileByUserIdAndProjectId(auth.getUser().getId(),
				project.getId());
			Retro retro = testDataFactory.createRetroWithParticipants(project, List.of(authProfile));
			NoteParticipantUpdateReqDto reqDto = new NoteParticipantUpdateReqDto(
				List.of()
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/retros/{retroId}", url, retro.getId())
						.cookie(new Cookie("accessToken", otherAuth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트의 Retro 참가자 업데이트 실패")
		public void updateRetroParticipantsNotFoundFail() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non-existent-url";
			NoteParticipantUpdateReqDto reqDto = new NoteParticipantUpdateReqDto(
				List.of()
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/retros/{retroId}", url, 1L)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(reqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 Retro 참가자 업데이트 실패")
		public void updateRetroParticipantsRetroNotFoundFail() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Long nonExistentRetroId = 9999L;
			NoteParticipantUpdateReqDto reqDto = new NoteParticipantUpdateReqDto(
				List.of()
			);

			// when
			String response = mockMvc.perform(
					patch("/api/v1/projects/{projectUrl}/retros/{retroId}", url, nonExistentRetroId)
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
