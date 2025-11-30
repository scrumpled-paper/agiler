package scrumpledpaper.agiler.kanban;

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
import scrumpledpaper.agiler.fixture.KanbanConfigFixture;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.kanban.dto.KanbanConfigUpdateReqDto;
import scrumpledpaper.agiler.kanban.entity.DefaultKanbanConfig;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.kanban.entity.KanbanConfigSnapshot;
import scrumpledpaper.agiler.project.entity.Project;

@IntegrationTest
@Transactional
public class KanbanConfigControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private TestDataFactory testDataFactory;
	Image defaultImage;

	@Nested
	@DisplayName("Kanban Config Update API")
	class UpdateKanbanConfig {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("204 - Kanban Config 수정 성공")
		public void updateKanbanConfigSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			testDataFactory.defaultKanbanConfigSet(project);
			int count = 5;
			KanbanConfigUpdateReqDto updateReqDto = KanbanConfigFixture.createUpdateReqDto(count);

			// when
			mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/kanban-config", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateReqDto)))
				.andExpect(status().isNoContent())
				.andReturn().getResponse().getContentAsString();

			// then
			List<KanbanConfig> kanbanConfigs = testDataFactory.getKanbanConfigsByProject(project);
			assertThat(kanbanConfigs).hasSize(count);

			for (int i = 0; i < count; i++) {
				KanbanConfigUpdateReqDto.KanbanConfigReqDto reqDto = updateReqDto.kanbanConfigs().get(i);
				KanbanConfig kanbanConfig = kanbanConfigs.get(i);

				assertThat(kanbanConfig.getStatusName()).isEqualTo(reqDto.statusName());
				assertThat(kanbanConfig.getPriority()).isEqualTo(reqDto.priority());
				assertThat(kanbanConfig.isDefaultStatus()).isEqualTo(reqDto.defaultStatus());
				assertThat(kanbanConfig.isBacklog()).isEqualTo(reqDto.backlog());
				assertThat(kanbanConfig.getIsDone()).isEqualTo(reqDto.isDone());
			}

			List<KanbanConfigSnapshot> kanbanConfigSnapshots = testDataFactory.findKanbanConfigSnapshotsByProjectId(project.getId());
			assertThat(kanbanConfigSnapshots).hasSize(DefaultKanbanConfig.values().length);
		}

		@Test
		@DisplayName("400 - 우선순위 중복으로 인한 Kanban Config 수정 실패 ")
		public void updateKanbanConfigFail_DuplicatePriority() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			KanbanConfigUpdateReqDto updateReqDto = KanbanConfigFixture.createUpdateReqDto(5);
			List<KanbanConfigUpdateReqDto.KanbanConfigReqDto> configs = updateReqDto.kanbanConfigs();
			configs.set(4, new KanbanConfigUpdateReqDto.KanbanConfigReqDto(
				configs.get(4).statusName(),
				configs.get(0).priority(),
				configs.get(4).defaultStatus(),
				configs.get(4).backlog(),
				configs.get(4).isDone()
			));
			updateReqDto = new KanbanConfigUpdateReqDto(configs);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/kanban-config", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateReqDto)))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.DUPLICATE_KANBAN_CONFIG_PRIORITY.getMessage());
		}

		@Test
		@DisplayName("400 - Default값이 없음으로 인한 Kanban Config 수정 실패 ")
		public void updateKanbanConfigFail_InvalidDefaultStatus() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			KanbanConfigUpdateReqDto updateReqDto = KanbanConfigFixture.createUpdateReqDto(5);
			List<KanbanConfigUpdateReqDto.KanbanConfigReqDto> configs = updateReqDto.kanbanConfigs();
			configs.set(1, new KanbanConfigUpdateReqDto.KanbanConfigReqDto(
				configs.get(1).statusName(),
				configs.get(1).priority(),
				false,
				configs.get(1).backlog(),
				configs.get(1).isDone()
			));
			updateReqDto = new KanbanConfigUpdateReqDto(configs);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/kanban-config", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateReqDto)))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.INVALID_KANBAN_CONFIG_DEFAULT_STATUS.getMessage());
		}

		@Test
		@DisplayName("400 - Backlog값이 없음으로 인한 Kanban Config 수정 실패 ")
		public void updateKanbanConfigFail_InvalidBacklogStatus() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			KanbanConfigUpdateReqDto updateReqDto = KanbanConfigFixture.createUpdateReqDto(5);
			List<KanbanConfigUpdateReqDto.KanbanConfigReqDto> configs = updateReqDto.kanbanConfigs();
			configs.set(0, new KanbanConfigUpdateReqDto.KanbanConfigReqDto(
				configs.get(0).statusName(),
				configs.get(0).priority(),
				configs.get(0).defaultStatus(),
				false,
				configs.get(0).isDone()
			));
			updateReqDto = new KanbanConfigUpdateReqDto(configs);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/kanban-config", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateReqDto)))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.INVALID_KANBAN_CONFIG_BACKLOG_STATUS.getMessage());
		}

		@Test
		@DisplayName("400 - Done값이 없음으로 인한 Kanban Config 수정 실패 ")
		public void updateKanbanConfigFail_InvalidDoneStatus() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			KanbanConfigUpdateReqDto updateReqDto = KanbanConfigFixture.createUpdateReqDto(5);
			List<KanbanConfigUpdateReqDto.KanbanConfigReqDto> configs = updateReqDto.kanbanConfigs();
			configs.set(2, new KanbanConfigUpdateReqDto.KanbanConfigReqDto(
				configs.get(2).statusName(),
				configs.get(2).priority(),
				configs.get(2).defaultStatus(),
				configs.get(2).backlog(),
				false
			));
			updateReqDto = new KanbanConfigUpdateReqDto(configs);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/kanban-config", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateReqDto)))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.INVALID_KANBAN_CONFIG_DONE_STATUS.getMessage());
		}

		@Test
		@DisplayName("403 - 권한 없음으로 인한 Kanban Config 수정 실패 ")
		public void updateKanbanConfigFail_Forbidden() throws Exception {
			// given
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			AuthContext otherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());
			KanbanConfigUpdateReqDto updateReqDto = KanbanConfigFixture.createUpdateReqDto(5);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/kanban-config", url)
						.cookie(new Cookie("accessToken", otherAuth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateReqDto)))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트로 인한 Kanban Config 수정 실패 ")
		public void updateKanbanConfigFail_ProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			KanbanConfigUpdateReqDto updateReqDto = KanbanConfigFixture.createUpdateReqDto(5);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}/kanban-config", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateReqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("Kanban Config List GET API")
	class GetKanbanConfigList {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - Kanban Config 리스트 조회 성공")
		public void getKanbanConfigListSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			int count = 5;
		    List<KanbanConfig> kanbanConfigs = testDataFactory.createKanbanConfigs(project, count);

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/kanban-config", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			for (KanbanConfig kanbanConfig : kanbanConfigs) {
				assertThat(response).contains(kanbanConfig.getStatusName());
				assertThat(response).contains(String.valueOf(kanbanConfig.getPriority()));
				assertThat(response).contains(String.valueOf(kanbanConfig.isDefaultStatus()));
				assertThat(response).contains(String.valueOf(kanbanConfig.isBacklog()));
				assertThat(response).contains(String.valueOf(kanbanConfig.getIsDone()));
			}
		}

		@Test
		@DisplayName("403 - 권한 없음으로 인한 Kanban Config 리스트 조회 실패 ")
		public void getKanbanConfigListFail_Forbidden() throws Exception {
			// given
			AuthContext ownerAuth = testDataFactory.createAuth(defaultImage);
			AuthContext otherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";
			testDataFactory.createProjectAndOwnerProfile(url, ownerAuth.getUser());

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/kanban-config", url)
						.cookie(new Cookie("accessToken", otherAuth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getMessage());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트로 인한 Kanban Config 리스트 조회 실패 ")
		public void getKanbanConfigListFail_ProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url";

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}/kanban-config", url)
						.cookie(new Cookie("accessToken", auth.getToken()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getMessage());
		}
	}
}
