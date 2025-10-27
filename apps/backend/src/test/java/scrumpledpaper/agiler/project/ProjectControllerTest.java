package scrumpledpaper.agiler.project;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import scrumpledpaper.agiler.annotation.IntegrationTest;
import scrumpledpaper.agiler.common.AuthContext;
import scrumpledpaper.agiler.common.PageResDto;
import scrumpledpaper.agiler.common.TestDataFactory;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.fixture.ProjectFixture;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.project.dto.ProjectCheckResDto;
import scrumpledpaper.agiler.project.dto.ProjectCreateReqDto;
import scrumpledpaper.agiler.project.dto.ProjectCreateResDto;
import scrumpledpaper.agiler.project.dto.ProjectInfoResDto;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.repository.ProjectRepository;
import scrumpledpaper.agiler.user.entity.Profile;
import scrumpledpaper.agiler.user.entity.Role;
import scrumpledpaper.agiler.user.repository.ProfileRepository;

@IntegrationTest
public class ProjectControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private ProfileRepository profileRepository;
	@Autowired
	private TestDataFactory testDataFactory;
	Image defaultImage;

	@Nested
	@DisplayName("Create Project Test")
	class CreateProjectTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("201 - Project Create Success")
		public void projectCreateSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			ProjectCreateReqDto createReqDto = ProjectFixture.createProjectCreateReqDto();
			String updateJson = objectMapper.writeValueAsString(createReqDto);
			// when
			String res = mockMvc.perform(
					post("/api/v1/projects")
						.header("Authorization", auth.bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
			// then
			ProjectCreateResDto projectCreateResDto = objectMapper.readValue(res, ProjectCreateResDto.class);
			Project createdProject = projectRepository.findById(projectCreateResDto.id())
				.orElseThrow();
			assertThat(projectCreateResDto.id()).isEqualTo(createdProject.getId());

			assertThat(createdProject.getTitle()).isEqualTo(createReqDto.title());
			assertThat(createdProject.getUrl()).isEqualTo(createReqDto.url());
			assertThat(createdProject.getSummary()).isEqualTo(createReqDto.summary());

			Profile ownerProfile = profileRepository.findByUserIdAndProjectId(auth.getUser().getId(), createdProject.getId())
				.orElseThrow();
			assertThat(ownerProfile.getRole()).isEqualTo(Role.OWNER);
			assertThat(ownerProfile.getEmail()).isEqualTo(auth.getUser().getEmail());
			assertThat(ownerProfile.getNickname()).isEqualTo(auth.getUser().getNickname());
			assertThat(ownerProfile.getImageId()).isEqualTo(auth.getUser().getImageId());
		}

		@Test
		@DisplayName("404 - User Not Found")
		public void notFoundUser() throws Exception {
			// given
			String accessToken = testDataFactory.createNotAllowedAccessToken();
			ProjectCreateReqDto createReqDto = ProjectFixture.createProjectCreateReqDto();
			String updateJson = objectMapper.writeValueAsString(createReqDto);
			// when
			String res = mockMvc.perform(
					post("/api/v1/projects")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();
			// then
			assertThat(res).contains(ErrorCode.USER_NOT_FOUND.getCode());
		}

		@Test
		@DisplayName("409 - Duplicate Project URL")
		public void duplicateUrl() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			ProjectCreateReqDto createReqDto = ProjectFixture.createProjectCreateReqDto();
			testDataFactory.createProject(createReqDto.url());
			String updateJson = objectMapper.writeValueAsString(createReqDto);
			// when
			String res = mockMvc.perform(
					post("/api/v1/projects")
						.header("Authorization", auth.bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isConflict())
				.andReturn().getResponse().getContentAsString();
			// then
			assertThat(res).contains(ErrorCode.PROJECT_URL_ALREADY_EXISTS.getCode());
		}
	}

	@Nested
	@DisplayName("Check Project URL Test")
	class CheckProjectUrlAndTagTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - Already Project URL Check Success")
		public void alreadyProjectUrlCheckSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url_tag";
			testDataFactory.createProject(url);
			// when
			String res = mockMvc.perform(
					get("/api/v1/projects/check")
						.header("Authorization", auth.bearer())
						.param("url", url)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
			// then
			ProjectCheckResDto projectCheckResDto = objectMapper.readValue(res, ProjectCheckResDto.class);
			assertThat(projectCheckResDto.isDuplicated()).isTrue();
		}

		@Test
		@DisplayName("200 - Project URL Check Success")
		public void ProjectUrlCheckSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url_tag";
			// when
			String res = mockMvc.perform(
					get("/api/v1/projects/check")
						.header("Authorization", auth.bearer())
						.param("url", url)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
			// then
			ProjectCheckResDto projectCheckResDto = objectMapper.readValue(res, ProjectCheckResDto.class);
			assertThat(projectCheckResDto.isDuplicated()).isFalse();
		}

		@ParameterizedTest
		@ValueSource(strings = {
			"test_url_tag",
			"test__tag",
			"_tag",
			"test_",
			"test",
			"test#tag",
			"test tag",
			"test+tag_name",
			"",
			"very-long-project-name-here_very-long-tag-name-here-too"
		})
		@DisplayName("400 - Invalid Project URL Format")
		void invalidProjectUrlFormat(String invalidUrl) throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			// when & then
			mockMvc.perform(
					get("/api/v1/projects/check")
						.header("Authorization", auth.bearer())
						.param("url", invalidUrl))
				.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("Get Project Info Pagination Test")
	class GetProjectInfoPaginationTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 첫 번째 페이지 10개 조회")
		void getFirstPage() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			testDataFactory.createProjects(auth.getUser(), "project-url", 25);
			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/info")
						.header("Authorization", auth.bearer())
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<ProjectInfoResDto> page = objectMapper.readValue(response,
				new TypeReference<PageResDto<ProjectInfoResDto>>() {
				});
			assertThat(page.getPageSize()).isEqualTo(10);
			assertThat(page.getCurrentPage()).isEqualTo(0);
			assertThat(page.getTotalPages()).isEqualTo(3);
			assertThat(page.getTotalElements()).isEqualTo(25);
			assertThat(page.getContents()).hasSize(10);
		}

		@Test
		@DisplayName("200 - 두 번째 페이지 10개 조회")
		void getSecondPage() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			testDataFactory.createProjects(auth.getUser(), "project-url", 25);
			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/info")
						.header("Authorization", auth.bearer())
						.param("page", "1")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<ProjectInfoResDto> page = objectMapper.readValue(response,
				new TypeReference<PageResDto<ProjectInfoResDto>>() {
				});
			assertThat(page.getPageSize()).isEqualTo(10);
			assertThat(page.getCurrentPage()).isEqualTo(1);
			assertThat(page.getTotalPages()).isEqualTo(3);
			assertThat(page.getTotalElements()).isEqualTo(25);
			assertThat(page.getContents()).hasSize(10);
		}

		@Test
		@DisplayName("200 - 마지막 페이지 5개 조회")
		void getThirdPage() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			testDataFactory.createProjects(auth.getUser(), "project-url", 25);
			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/info")
						.header("Authorization", auth.bearer())
						.param("page", "2")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<ProjectInfoResDto> page = objectMapper.readValue(response,
				new TypeReference<PageResDto<ProjectInfoResDto>>() {
				});
			assertThat(page.getPageSize()).isEqualTo(10);
			assertThat(page.getCurrentPage()).isEqualTo(2);
			assertThat(page.getTotalPages()).isEqualTo(3);
			assertThat(page.getTotalElements()).isEqualTo(25);
			assertThat(page.getContents()).hasSize(5);
		}

		@Test
		@DisplayName("200 - Default Size 페이지 조회")
		void getDefaultSizePage() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			testDataFactory.createProjects(auth.getUser(), "project-url", 25);
			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/info")
						.header("Authorization", auth.bearer())
						.param("page", "1"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<ProjectInfoResDto> page = objectMapper.readValue(response,
				new TypeReference<PageResDto<ProjectInfoResDto>>() {
				});
			assertThat(page.getPageSize()).isEqualTo(10);
			assertThat(page.getCurrentPage()).isEqualTo(1);
			assertThat(page.getTotalPages()).isEqualTo(3);
			assertThat(page.getTotalElements()).isEqualTo(25);
			assertThat(page.getContents()).hasSize(10);
		}

		@Test
		@DisplayName("200 - 프로젝트가 없을 때 조회")
		void getNoContentPage() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/info")
						.header("Authorization", auth.bearer())
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<ProjectInfoResDto> page = objectMapper.readValue(response,
				new TypeReference<PageResDto<ProjectInfoResDto>>() {
				});
			assertThat(page.getPageSize()).isEqualTo(10);
			assertThat(page.getCurrentPage()).isEqualTo(0);
			assertThat(page.getTotalPages()).isEqualTo(0);
			assertThat(page.getTotalElements()).isEqualTo(0);
			assertThat(page.getContents()).hasSize(0);
		}

		@Test
		@DisplayName("404 - 범위 밖의 페이지 조회")
		void getNotExistPage() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			testDataFactory.createProjects(auth.getUser(), "project-url", 25);
			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/info")
						.header("Authorization", auth.bearer())
						.param("page", "3")
						.param("size", "10"))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();
			// then
			assertThat(response).contains(ErrorCode.PAGE_NOT_FOUND.getCode());
		}

		@Test
		@DisplayName("200 - 최신순 정렬 확인")
		void checkSortByCreatedAtDesc() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			LocalDateTime baseTime = LocalDateTime.now();

			testDataFactory.createProjectWithTime("project_1", auth.getUser(), baseTime.minusDays(4));
			testDataFactory.createProjectWithTime("project_2", auth.getUser(), baseTime.minusDays(3));
			testDataFactory.createProjectWithTime("project_3", auth.getUser(), baseTime.minusDays(2));
			testDataFactory.createProjectWithTime("project_4", auth.getUser(), baseTime.minusDays(1));
			testDataFactory.createProjectWithTime("project_5", auth.getUser(), baseTime);

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/info")
						.header("Authorization", auth.bearer())
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<ProjectInfoResDto> page = objectMapper.readValue(
				response,
				new TypeReference<PageResDto<ProjectInfoResDto>>() {}
			);

			assertThat(page.getContents()).hasSize(5);
			assertThat(page.getContents().get(0).url()).isEqualTo("project_5");
			assertThat(page.getContents().get(1).url()).isEqualTo("project_4");
			assertThat(page.getContents().get(2).url()).isEqualTo("project_3");
			assertThat(page.getContents().get(3).url()).isEqualTo("project_2");
			assertThat(page.getContents().get(4).url()).isEqualTo("project_1");
		}

		@Test
		@DisplayName("200 - 여러 페이지 정렬 일관성 확인")
		void checkSortingConsistency() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			LocalDateTime baseTime = LocalDateTime.now();

			for (int i = 1; i <= 25; i++) {
				testDataFactory.createProjectWithTime(
					"project_" + i,
					auth.getUser(),
					baseTime.minusHours(25 - i)
				); // 25가 가장 최신
			}

			// when
			PageResDto<ProjectInfoResDto> page1 = objectMapper.readValue(
				mockMvc.perform(
						get("/api/v1/projects/info")
							.header("Authorization", auth.bearer())
							.param("page", "0")
							.param("size", "10"))
					.andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString(),
				new TypeReference<PageResDto<ProjectInfoResDto>>() {}
			);

			PageResDto<ProjectInfoResDto> page2 = objectMapper.readValue(
				mockMvc.perform(
						get("/api/v1/projects/info")
							.header("Authorization", auth.bearer())
							.param("page", "1")
							.param("size", "10"))
					.andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString(),
				new TypeReference<PageResDto<ProjectInfoResDto>>() {}
			);

			PageResDto<ProjectInfoResDto> page3 = objectMapper.readValue(
				mockMvc.perform(
						get("/api/v1/projects/info")
							.header("Authorization", auth.bearer())
							.param("page", "2")
							.param("size", "10"))
					.andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString(),
				new TypeReference<PageResDto<ProjectInfoResDto>>() {}
			);

			// then
			assertThat(page1.getContents()).hasSize(10);
			assertThat(page2.getContents()).hasSize(10);
			assertThat(page3.getContents()).hasSize(5);

			List<ProjectInfoResDto> allProjects = new ArrayList<>();
			allProjects.addAll(page1.getContents());
			allProjects.addAll(page2.getContents());
			allProjects.addAll(page3.getContents());
			assertThat(allProjects.getFirst().url()).isEqualTo("project_25");
			assertThat(allProjects.get(24).url()).isEqualTo("project_1");
		}

		@Test
		@DisplayName("200 - 다른 유저가 만든 프로젝트에 참가 시점으로 정렬")
		void sortByProfileCreatedAt() throws Exception {
			// given
			AuthContext owner = testDataFactory.createAuth(defaultImage);
			LocalDateTime baseTime = LocalDateTime.now();

			Project project1 = testDataFactory.createProjectWithTime("project_1", owner.getUser(), baseTime.minusDays(10));
			Project project2 = testDataFactory.createProjectWithTime("project_2", owner.getUser(), baseTime.minusDays(9));
			Project project3 = testDataFactory.createProjectWithTime("project_3", owner.getUser(), baseTime.minusDays(8));

			AuthContext member = testDataFactory.createAuth(testDataFactory.createDefaultImage());

			testDataFactory.createProfileWithTime(member.getUser(), project3, Role.MEMBER, baseTime.minusDays(5));
			testDataFactory.createProfileWithTime(member.getUser(), project2, Role.MEMBER, baseTime.minusDays(3));
			testDataFactory.createProfileWithTime(member.getUser(), project1, Role.MEMBER, baseTime.minusDays(4));
			// 프로필이 가장 최근에 생성된 프로젝트가 가장 앞에 와야 함 (2, 1, 3) 순서대로

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/info")
						.header("Authorization", member.bearer())
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<ProjectInfoResDto> page = objectMapper.readValue(
				response,
				new TypeReference<PageResDto<ProjectInfoResDto>>() {}
			);
			assertThat(page.getContents().get(0).url()).isEqualTo("project_2");
			assertThat(page.getContents().get(1).url()).isEqualTo("project_1");
			assertThat(page.getContents().get(2).url()).isEqualTo("project_3");
		}
	}
}
