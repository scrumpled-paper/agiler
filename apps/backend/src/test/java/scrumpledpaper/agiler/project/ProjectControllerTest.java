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
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import scrumpledpaper.agiler.annotation.IntegrationTest;
import scrumpledpaper.agiler.common.AuthContext;
import scrumpledpaper.agiler.common.PageResDto;
import scrumpledpaper.agiler.common.TestDataFactory;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.fixture.ProjectFixture;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.project.dto.ProfileResDto;
import scrumpledpaper.agiler.project.dto.ProjectCheckResDto;
import scrumpledpaper.agiler.project.dto.ProjectCreateReqDto;
import scrumpledpaper.agiler.project.dto.ProjectDetailResDto;
import scrumpledpaper.agiler.project.dto.ProjectIdResDto;
import scrumpledpaper.agiler.project.dto.ProjectInfoResDto;
import scrumpledpaper.agiler.project.dto.ProjectSideResDto;
import scrumpledpaper.agiler.project.dto.ProjectUpdateReqDto;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Role;

@IntegrationTest
@Transactional
public class ProjectControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
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
			String response = mockMvc.perform(
					post("/api/v1/projects")
						.header("Authorization", auth.bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
			// then
			ProjectIdResDto projectIdResDto = objectMapper.readValue(response, ProjectIdResDto.class);
			Project createdProject = testDataFactory.findProjectById(projectIdResDto.id());
			assertThat(projectIdResDto.id()).isEqualTo(createdProject.getId());

			assertThat(createdProject.getTitle()).isEqualTo(createReqDto.title());
			assertThat(createdProject.getUrl()).isEqualTo(createReqDto.url());
			assertThat(createdProject.getSummary()).isEqualTo(createReqDto.summary());

			Profile ownerProfile = testDataFactory.findProfileByUserIdAndProjectId(
				auth.getUser().getId(),
				createdProject.getId()
			);
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
			String response = mockMvc.perform(
					post("/api/v1/projects")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();
			// then
			assertThat(response).contains(ErrorCode.USER_NOT_FOUND.getCode());
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
			String response = mockMvc.perform(
					post("/api/v1/projects")
						.header("Authorization", auth.bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateJson))
				.andExpect(status().isConflict())
				.andReturn().getResponse().getContentAsString();
			// then
			assertThat(response).contains(ErrorCode.PROJECT_URL_ALREADY_EXISTS.getCode());
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
			String response = mockMvc.perform(
					get("/api/v1/projects/check")
						.header("Authorization", auth.bearer())
						.param("url", url)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
			// then
			ProjectCheckResDto projectCheckResDto = objectMapper.readValue(response, ProjectCheckResDto.class);
			assertThat(projectCheckResDto.isDuplicated()).isTrue();
		}

		@Test
		@DisplayName("200 - Project URL Check Success")
		public void ProjectUrlCheckSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test-url_tag";
			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/check")
						.header("Authorization", auth.bearer())
						.param("url", url)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
			// then
			ProjectCheckResDto projectCheckResDto = objectMapper.readValue(response, ProjectCheckResDto.class);
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
						.param("page", "10")
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
				new TypeReference<PageResDto<ProjectInfoResDto>>() {
				}
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
				new TypeReference<PageResDto<ProjectInfoResDto>>() {
				}
			);

			PageResDto<ProjectInfoResDto> page2 = objectMapper.readValue(
				mockMvc.perform(
						get("/api/v1/projects/info")
							.header("Authorization", auth.bearer())
							.param("page", "1")
							.param("size", "10"))
					.andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString(),
				new TypeReference<PageResDto<ProjectInfoResDto>>() {
				}
			);

			PageResDto<ProjectInfoResDto> page3 = objectMapper.readValue(
				mockMvc.perform(
						get("/api/v1/projects/info")
							.header("Authorization", auth.bearer())
							.param("page", "2")
							.param("size", "10"))
					.andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString(),
				new TypeReference<PageResDto<ProjectInfoResDto>>() {
				}
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

			Project project1 = testDataFactory.createProjectWithTime("project_1", owner.getUser(),
				baseTime.minusDays(10));
			Project project2 = testDataFactory.createProjectWithTime("project_2", owner.getUser(),
				baseTime.minusDays(9));
			Project project3 = testDataFactory.createProjectWithTime("project_3", owner.getUser(),
				baseTime.minusDays(8));

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
				new TypeReference<PageResDto<ProjectInfoResDto>>() {
				}
			);
			assertThat(page.getContents().get(0).url()).isEqualTo("project_2");
			assertThat(page.getContents().get(1).url()).isEqualTo("project_1");
			assertThat(page.getContents().get(2).url()).isEqualTo("project_3");
		}

		@ParameterizedTest
		@ValueSource(strings = {
			"createdAt"
		})
		@DisplayName("200 - @Valid 검증 통과")
		void validatePageReqDto(String sort) throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			// when & then
			mockMvc.perform(
					get("/api/v1/projects/info")
						.header("Authorization", auth.bearer())
						.param("page", "0")
						.param("size", "10")
						.param("sort", sort))
				.andExpect(status().isOk());
		}

		@ParameterizedTest
		@ValueSource(strings = {
			"CREATEDAT",
			"CreatedAt",
			"createdat",
			"createdat",
			"NonExistingField",
			"12345",
			"",
			"created-at"
		})
		@DisplayName("400 - @Valid 검증 실패")
		void invalidatePageReqDto(String sort) throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/info")
						.header("Authorization", auth.bearer())
						.param("page", "0")
						.param("size", "10")
						.param("sort", sort))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getContentAsString();
			// then
			assertThat(response).contains(ErrorCode.INVALID_REQUEST.getCode());
		}
	}

	@Nested
	@DisplayName("Get Project Side Pagination Test")
	class GetProjectSidePaginationTest {
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
					get("/api/v1/projects")
						.header("Authorization", auth.bearer())
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<ProjectSideResDto> page = objectMapper.readValue(response,
				new TypeReference<PageResDto<ProjectSideResDto>>() {
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
					get("/api/v1/projects")
						.header("Authorization", auth.bearer())
						.param("page", "1")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<ProjectSideResDto> page = objectMapper.readValue(response,
				new TypeReference<PageResDto<ProjectSideResDto>>() {
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
					get("/api/v1/projects")
						.header("Authorization", auth.bearer())
						.param("page", "2")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<ProjectSideResDto> page = objectMapper.readValue(response,
				new TypeReference<PageResDto<ProjectSideResDto>>() {
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
					get("/api/v1/projects")
						.header("Authorization", auth.bearer())
						.param("page", "1"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<ProjectSideResDto> page = objectMapper.readValue(response,
				new TypeReference<PageResDto<ProjectSideResDto>>() {
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
					get("/api/v1/projects")
						.header("Authorization", auth.bearer())
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<ProjectSideResDto> page = objectMapper.readValue(response,
				new TypeReference<PageResDto<ProjectSideResDto>>() {
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
					get("/api/v1/projects")
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
					get("/api/v1/projects")
						.header("Authorization", auth.bearer())
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<ProjectSideResDto> page = objectMapper.readValue(
				response,
				new TypeReference<PageResDto<ProjectSideResDto>>() {
				}
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
			PageResDto<ProjectSideResDto> page1 = objectMapper.readValue(
				mockMvc.perform(
						get("/api/v1/projects")
							.header("Authorization", auth.bearer())
							.param("page", "0")
							.param("size", "10"))
					.andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString(),
				new TypeReference<PageResDto<ProjectSideResDto>>() {
				}
			);

			PageResDto<ProjectSideResDto> page2 = objectMapper.readValue(
				mockMvc.perform(
						get("/api/v1/projects")
							.header("Authorization", auth.bearer())
							.param("page", "1")
							.param("size", "10"))
					.andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString(),
				new TypeReference<PageResDto<ProjectSideResDto>>() {
				}
			);

			PageResDto<ProjectSideResDto> page3 = objectMapper.readValue(
				mockMvc.perform(
						get("/api/v1/projects")
							.header("Authorization", auth.bearer())
							.param("page", "2")
							.param("size", "10"))
					.andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString(),
				new TypeReference<PageResDto<ProjectSideResDto>>() {
				}
			);

			// then
			assertThat(page1.getContents()).hasSize(10);
			assertThat(page2.getContents()).hasSize(10);
			assertThat(page3.getContents()).hasSize(5);

			List<ProjectSideResDto> allProjects = new ArrayList<>();
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

			Project project1 = testDataFactory.createProjectWithTime("project_1", owner.getUser(),
				baseTime.minusDays(10));
			Project project2 = testDataFactory.createProjectWithTime("project_2", owner.getUser(),
				baseTime.minusDays(9));
			Project project3 = testDataFactory.createProjectWithTime("project_3", owner.getUser(),
				baseTime.minusDays(8));

			AuthContext member = testDataFactory.createAuth(testDataFactory.createDefaultImage());

			testDataFactory.createProfileWithTime(member.getUser(), project3, Role.MEMBER, baseTime.minusDays(5));
			testDataFactory.createProfileWithTime(member.getUser(), project2, Role.MEMBER, baseTime.minusDays(3));
			testDataFactory.createProfileWithTime(member.getUser(), project1, Role.MEMBER, baseTime.minusDays(4));
			// 프로필이 가장 최근에 생성된 프로젝트가 가장 앞에 와야 함 (2, 1, 3) 순서대로

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects")
						.header("Authorization", member.bearer())
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			PageResDto<ProjectSideResDto> page = objectMapper.readValue(
				response,
				new TypeReference<PageResDto<ProjectSideResDto>>() {
				}
			);

			assertThat(page.getContents().get(0).url()).isEqualTo("project_2");
			assertThat(page.getContents().get(1).url()).isEqualTo("project_1");
			assertThat(page.getContents().get(2).url()).isEqualTo("project_3");
		}

		@ParameterizedTest
		@ValueSource(strings = {"createdAt"})
		@DisplayName("200 - @Valid 검증 통과")
		void validatePageReqDto(String sort) throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);

			// when & then
			mockMvc.perform(
					get("/api/v1/projects")
						.header("Authorization", auth.bearer())
						.param("page", "0")
						.param("size", "10")
						.param("sort", sort))
				.andExpect(status().isOk());
		}

		@ParameterizedTest
		@ValueSource(strings = {
			"CREATEDAT",
			"CreatedAt",
			"createdat",
			"NonExistingField",
			"12345",
			"",
			"created-at"
		})
		@DisplayName("400 - @Valid 검증 실패")
		void invalidatePageReqDto(String sort) throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects")
						.header("Authorization", auth.bearer())
						.param("page", "0")
						.param("size", "10")
						.param("sort", sort))
				.andExpect(status().isBadRequest())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.INVALID_REQUEST.getCode());
		}
	}

	@Nested
	@DisplayName("Project Detail Test")
	class ProjectDetailTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 이미지가 있는 프로젝트 상세 조회 성공")
		public void projectDetailSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String imageUrl = "https://example.com/notdefault.png";
			Project project = testDataFactory.createProjectWithImageUrl(imageUrl);
			testDataFactory.createProfile(auth.getUser(), project, Role.OWNER);

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}", project.getUrl())
						.header("Authorization", auth.bearer())
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			ProjectDetailResDto projectDetailResDto = objectMapper.readValue(response, ProjectDetailResDto.class);
			assertThat(projectDetailResDto.title()).isEqualTo(project.getTitle());
			assertThat(projectDetailResDto.summary()).isEqualTo(project.getSummary());
			Image image = testDataFactory.findImageById(project.getImageId());
			assertThat(projectDetailResDto.imageUrl()).isEqualTo(image.getUrl());
		}

		@Test
		@DisplayName("200 - 이미지가 없는 프로젝트 상세 조회 성공")
		public void projectDetailNoImageSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			Project project = testDataFactory.createProject();
			testDataFactory.createProfile(auth.getUser(), project, Role.MEMBER);

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}", project.getUrl())
						.header("Authorization", auth.bearer())
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			ProjectDetailResDto projectDetailResDto = objectMapper.readValue(response, ProjectDetailResDto.class);
			assertThat(projectDetailResDto.title()).isEqualTo(project.getTitle());
			assertThat(projectDetailResDto.summary()).isEqualTo(project.getSummary());
			assertThat(projectDetailResDto.imageUrl()).isEqualTo("");
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트 상세 조회 실패")
		public void projectDetailNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String nonExistingProjectUrl = "non-existing_project-url";

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}", nonExistingProjectUrl)
						.header("Authorization", auth.bearer())
				)
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getCode());
		}

		@Test
		@DisplayName("403 - 권한 없는 프로젝트 상세 조회 실패")
		public void projectDetailForbidden() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			Project project = testDataFactory.createProject();

			// when
			String response = mockMvc.perform(
					get("/api/v1/projects/{projectUrl}", project.getUrl())
						.header("Authorization", auth.bearer())
				)
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getCode());
		}
	}

	@Nested
	@DisplayName("Project Update Test")
	class ProjectUpdateTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = testDataFactory.createDefaultImage();
		}

		@Test
		@DisplayName("200 - 프로젝트 수정 성공")
		void updateProjectSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			ProjectUpdateReqDto updateReqDto = new ProjectUpdateReqDto(
				"새로운 제목",
				"another_url",
				"새로운 요약"
			);
			String requestBody = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}", url)
						.header("Authorization", auth.bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			ProjectIdResDto result = objectMapper.readValue(response, ProjectIdResDto.class);
			assertThat(result.id()).isEqualTo(project.getId());

			Project updated = testDataFactory.findProjectById(project.getId());
			assertThat(updated.getTitle()).isEqualTo(updateReqDto.title());
			assertThat(updated.getUrl()).isEqualTo(updateReqDto.url());
			assertThat(updated.getSummary()).isEqualTo(updateReqDto.summary());
		}

		@Test
		@DisplayName("200 - 같은 URL 수정 성공")
		void updateProjectSameUrlSuccess() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			ProjectUpdateReqDto updateReqDto = new ProjectUpdateReqDto(
				"새로운 제목",
				"test_url",
				"새로운 요약"
			);
			String requestBody = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}", url)
						.header("Authorization", auth.bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

			// then
			ProjectIdResDto result = objectMapper.readValue(response, ProjectIdResDto.class);
			assertThat(result.id()).isEqualTo(project.getId());

			Project updated = testDataFactory.findProjectById(project.getId());
			assertThat(updated.getTitle()).isEqualTo(updateReqDto.title());
			assertThat(updated.getUrl()).isEqualTo(updateReqDto.url());
			assertThat(updated.getSummary()).isEqualTo(updateReqDto.summary());
		}

		@Test
		@DisplayName("404 - 존재하지 않는 프로젝트")
		void updateProjectNotFound() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			ProjectUpdateReqDto updateReqDto = new ProjectUpdateReqDto(
				"새로운 제목",
				"test_url",
				"새로운 요약"
			);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}", "non-exist_url")
						.header("Authorization", auth.bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateReqDto)))
				.andExpect(status().isNotFound())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_FOUND.getCode());
		}

		@Test
		@DisplayName("403 - 프로젝트 멤버가 아님")
		void updateProjectNotMember() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext anotherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			ProjectUpdateReqDto updateReqDto = new ProjectUpdateReqDto(
				"새로운 제목",
				"another_url",
				"새로운 요약"
			);
			String requestBody = objectMapper.writeValueAsString(updateReqDto);

			// when & then
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}", url)
						.header("Authorization", anotherAuth.bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateReqDto)))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_NOT_MEMBER.getCode());
		}

		@Test
		@DisplayName("403 - 프로젝트 오너가 아님")
		void updateProjectNotOwner() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			AuthContext anotherAuth = testDataFactory.createAuth(defaultImage);
			String url = "test_url";
			Project project = testDataFactory.createProjectAndOwnerProfile(url, auth.getUser());
			Profile memberProfile = testDataFactory.createProfile(anotherAuth.getUser(), project, Role.MEMBER);
			ProjectUpdateReqDto updateReqDto = new ProjectUpdateReqDto(
				"새로운 제목",
				"another_url",
				"새로운 요약"
			);
			String requestBody = objectMapper.writeValueAsString(updateReqDto);

			// when & then
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}", url)
						.header("Authorization", anotherAuth.bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateReqDto)))
				.andExpect(status().isForbidden())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_OWNER_REQUIRED.getCode());
		}

		@Test
		@DisplayName("409 - 이미 존재하는 Project URL")
		void updateProjectDuplicateUrl() throws Exception {
			// given
			AuthContext auth = testDataFactory.createAuth(defaultImage);
			String url1 = "test_url-1";
			String url2 = "test_url-2";
			Project project1 = testDataFactory.createProjectAndOwnerProfile(url1, auth.getUser());
			Project project2 = testDataFactory.createProjectAndOwnerProfile(url2, auth.getUser());
			ProjectUpdateReqDto updateReqDto = new ProjectUpdateReqDto(
				"새로운 제목",
				url2,
				"새로운 요약"
			);
			String requestBody = objectMapper.writeValueAsString(updateReqDto);

			// when
			String response = mockMvc.perform(
					put("/api/v1/projects/{projectUrl}", url1)
						.header("Authorization", auth.bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isConflict())
				.andReturn().getResponse().getContentAsString();

			// then
			assertThat(response).contains(ErrorCode.PROJECT_URL_ALREADY_EXISTS.getCode());
		}
	}
}
