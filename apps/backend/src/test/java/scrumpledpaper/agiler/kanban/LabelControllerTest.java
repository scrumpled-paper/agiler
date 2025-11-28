package scrumpledpaper.agiler.kanban;

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
import scrumpledpaper.agiler.kanban.dto.LabelCreateReqDto;
import scrumpledpaper.agiler.kanban.dto.LabelDeleteReqDto;
import scrumpledpaper.agiler.kanban.entity.Label;
import scrumpledpaper.agiler.project.entity.Project;

@IntegrationTest
@Transactional
public class LabelControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private TestDataFactory testDataFactory;
	Image defaultImage;

	@Nested
	@DisplayName("Create Label Tests")
	class CreateLabelTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - 라벨 생성 성공")
		public void labelCreateSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			LabelCreateReqDto createReqDto = new LabelCreateReqDto("New Label", "Description", "#123456");
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/labels", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getContentAsString();

			// then
			List<Label> labels = testDataFactory.findLabelsByProjectId(project.getId());
			assertThat(labels)
				.anyMatch(label ->
					label.getName().equals(createReqDto.name()) &&
					label.getDescription().equals(createReqDto.description()) &&
					label.getColor().equals(createReqDto.color())
				);
		}

		@ParameterizedTest
		@DisplayName("400 - 형식에 맞지 않는 색상 코드로 라벨 생성 요청")
		@ValueSource(strings = {"123456", "#12345G", "#1234", "#1234567", "red", "#Z12345"})
		public void labelCreateInvalidColorCode(String colorCode) throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			LabelCreateReqDto createReqDto = new LabelCreateReqDto("New Label", "Description", colorCode);
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when & then
			mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/labels", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자가 라벨 생성 요청")
		public void labelCreateForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			AuthContext nonMemberAuth = testDataFactory.createAuth(defaultImage);
			LabelCreateReqDto createReqDto = new LabelCreateReqDto("New Label", "Description", "#123456");
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/labels", url)
						.cookie(new Cookie("accessToken", nonMemberAuth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 라벨 생성 요청")
		public void labelCreateProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non_existent_project";
			LabelCreateReqDto createReqDto = new LabelCreateReqDto("New Label", "Description", "#123456");
			String updateJson = objectMapper.writeValueAsString(createReqDto);

			// when
			String response = mockMvc.perform(
					post("/api/v1/projects/{projectUrl}/labels", url)
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
	@DisplayName("Get Labels Tests")
	class GetLabelsTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 라벨 목록 조회 성공")
		public void getLabelsSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Label label1 = testDataFactory.createLabel(project, "Label 1", "Description 1", "#111111");
			Label label2 = testDataFactory.createLabel(project, "Label 2", "Description 2", "#222222");

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/labels", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(label1.getName());
			assertThat(response).contains(label2.getName());
			assertThat(response).contains(label1.getColor());
			assertThat(response).contains(label2.getColor());
			assertThat(response).contains(label1.getDescription());
			assertThat(response).contains(label2.getDescription());
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자가 라벨 목록 조회 요청")
		public void getLabelsForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			AuthContext nonMemberAuth = testDataFactory.createAuth(defaultImage);

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/labels", url)
						.cookie(new Cookie("accessToken", nonMemberAuth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 라벨 목록 조회 요청")
		public void getLabelsProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non_existent_project";

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/labels", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("Update Label Tests")
	class UpdateLabelTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - 라벨 수정 성공")
		public void labelUpdateSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Label label = testDataFactory.createLabel(project, "Old Label", "Old Description", "#000000");
			LabelCreateReqDto updateReqDto = new LabelCreateReqDto("Updated Label", "Updated Description", "#FFFFFF");
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/labels/{labelId}", url, label.getId())
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNoContent());

			// then
			Label updatedLabel = testDataFactory.findLabelById(label.getId());
			assertThat(updatedLabel.getName()).isEqualTo(updateReqDto.name());
			assertThat(updatedLabel.getDescription()).isEqualTo(updateReqDto.description());
			assertThat(updatedLabel.getColor()).isEqualTo(updateReqDto.color());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 라벨 ID의 수정 요청")
		public void labelUpdateNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Long nonExistentLabelId = 9999L;
			LabelCreateReqDto updateReqDto = new LabelCreateReqDto("Updated Label", "Updated Description", "#FFFFFF");
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/labels/{labelId}", url, nonExistentLabelId)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.LABEL_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자가 라벨 수정 요청")
		public void labelUpdateForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Label label = testDataFactory.createLabel(project, "Old Label", "Old Description", "#000000");
			AuthContext nonMemberAuth = testDataFactory.createAuth(defaultImage);
			LabelCreateReqDto updateReqDto = new LabelCreateReqDto("Updated Label", "Updated Description", "#FFFFFF");
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/labels/{labelId}", url, label.getId())
						.cookie(new Cookie("accessToken", nonMemberAuth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 라벨 수정 요청")
		public void labelUpdateProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non_existent_project";
			Project project = testDataFactory.createProjectAndOwnerProfile("some_other_url", auth.getUser());
			Label label = testDataFactory.createLabel(project, "Old Label", "Old Description", "#000000");
			LabelCreateReqDto updateReqDto = new LabelCreateReqDto("Updated Label", "Updated Description", "#FFFFFF");
			String updateJson = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/labels/{labelId}", url, label.getId())
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
	@DisplayName("Delete Labels Tests")
	class DeleteLabelsTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - 라벨 삭제 성공")
		public void labelDeleteSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Label label = testDataFactory.createLabel(project, "Label 1", "Description 1", "#111111");
			LabelDeleteReqDto deleteReqDto = new LabelDeleteReqDto(label.getId());
			String deleteJson = objectMapper.writeValueAsString(deleteReqDto);

			// when
			mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/labels", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(deleteJson))
				.andExpect(status().isNoContent());

			// then
			List<Label> labels = testDataFactory.findLabelsByProjectId(project.getId());
			assertThat(labels).doesNotContain(label);
		}

		@Test
		@DisplayName("404 - 존재하지 않는 라벨 ID의 삭제 요청")
		public void labelDeleteNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Long nonExistentLabelId = 9999L;
			LabelDeleteReqDto deleteReqDto = new LabelDeleteReqDto(nonExistentLabelId);
			String deleteJson = objectMapper.writeValueAsString(deleteReqDto);

			// when
			String response = mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/labels", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(deleteJson))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.LABEL_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아닌 사용자가 라벨 삭제 요청")
		public void labelDeleteForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Label label = testDataFactory.createLabel(project, "Label 1", "Description 1", "#111111");
			AuthContext nonMemberAuth = testDataFactory.createAuth(defaultImage);
			LabelDeleteReqDto deleteReqDto = new LabelDeleteReqDto(label.getId());
			String deleteJson = objectMapper.writeValueAsString(deleteReqDto);

			// when
			String response = mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/labels", url)
						.cookie(new Cookie("accessToken", nonMemberAuth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(deleteJson))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트에 라벨 삭제 요청")
		public void labelDeleteProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "non_existent_project";
			Project project = testDataFactory.createProjectAndOwnerProfile("some_other_url", auth.getUser());
			Label label = testDataFactory.createLabel(project, "Label 1", "Description 1", "#111111");
			LabelDeleteReqDto deleteReqDto = new LabelDeleteReqDto(label.getId());
			String deleteJson = objectMapper.writeValueAsString(deleteReqDto);

			// when
			String response = mockMvc.perform(
					delete("/api/v1/projects/{projectUrl}/labels", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(deleteJson))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}
	}
}
