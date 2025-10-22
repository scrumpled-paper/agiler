package scrumpledpaper.agiler.project;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import scrumpledpaper.agiler.annotation.IntegrationTest;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.fixture.ImageFixture;
import scrumpledpaper.agiler.fixture.ProjectFixture;
import scrumpledpaper.agiler.fixture.TokenFixture;
import scrumpledpaper.agiler.fixture.UserFixture;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.image.repository.ImageRepository;
import scrumpledpaper.agiler.project.dto.ProjectCheckResDto;
import scrumpledpaper.agiler.project.dto.ProjectCreateReqDto;
import scrumpledpaper.agiler.project.dto.ProjectCreateResDto;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.repository.ProjectRepository;
import scrumpledpaper.agiler.user.entity.Profile;
import scrumpledpaper.agiler.user.entity.Role;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.repository.ProfileRepository;
import scrumpledpaper.agiler.user.repository.UserRepository;

@IntegrationTest
public class ProjectControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private TokenFixture tokenFixture;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ImageRepository imageRepository;
	@Autowired
	private ProfileRepository profileRepository;
	@Autowired
	private ProjectRepository projectRepository;
	Image defaultImage;

	@Nested
	@DisplayName("Create Project Test")
	class CreateProjectTest {
		@BeforeEach
		void beforeEach() {
			defaultImage = ImageFixture.createImage();
			imageRepository.save(defaultImage);
		}

		@Test
		@DisplayName("201 - Project Create Success")
		public void projectCreateSuccess() throws Exception {
			// given
			User user = UserFixture.createUser(defaultImage);
			userRepository.save(user);
			String accessToken = tokenFixture.createAccessToken(user);
			ProjectCreateReqDto createReqDto = ProjectFixture.createProjectCreateReqDto();
			String updateJson = objectMapper.writeValueAsString(createReqDto);
			// when
			String res = mockMvc.perform(
					post("/api/v1/projects")
						.header("Authorization", "Bearer " + accessToken)
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

			Profile ownerProfile = profileRepository.findByUserIdAndProjectId(user.getId(), createdProject.getId())
				.orElseThrow();
			assertThat(ownerProfile.getRole()).isEqualTo(Role.owner);
			assertThat(ownerProfile.getEmail()).isEqualTo(user.getEmail());
			assertThat(ownerProfile.getNickname()).isEqualTo(user.getNickname());
			assertThat(ownerProfile.getImageId()).isEqualTo(user.getImageId());
		}

		@Test
		@DisplayName("404 - User Not Found")
		public void notFoundUser() throws Exception {
			// given
			String accessToken = tokenFixture.createNotAllowedAccessToken();
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
			User user = UserFixture.createUser(defaultImage);
			userRepository.save(user);
			String accessToken = tokenFixture.createAccessToken(user);
			ProjectCreateReqDto createReqDto = ProjectFixture.createProjectCreateReqDto();
			String updateJson = objectMapper.writeValueAsString(createReqDto);
			Project existingProject = ProjectFixture.createProject(createReqDto.url());
			projectRepository.save(existingProject);
			// when
			String res = mockMvc.perform(
					post("/api/v1/projects")
						.header("Authorization", "Bearer " + accessToken)
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
			defaultImage = ImageFixture.createImage();
			imageRepository.save(defaultImage);
		}

		@Test
		@DisplayName("200 - Already Project URL Check Success")
		public void alreadyProjectUrlCheckSuccess() throws Exception {
			// given
			User user = UserFixture.createUser(defaultImage);
			userRepository.save(user);
			String accessToken = tokenFixture.createAccessToken(user);
			String url = "test-url_tag";
			Project existingProject = ProjectFixture.createProject(url);
			projectRepository.save(existingProject);
			// when
			String res = mockMvc.perform(
					get("/api/v1/projects/check")
						.header("Authorization", "Bearer " + accessToken)
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
			User user = UserFixture.createUser(defaultImage);
			userRepository.save(user);
			String accessToken = tokenFixture.createAccessToken(user);
			String url = "test-url_tag";
			// when
			String res = mockMvc.perform(
					get("/api/v1/projects/check")
						.header("Authorization", "Bearer " + accessToken)
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
			User user = UserFixture.createUser(defaultImage);
			userRepository.save(user);
			String accessToken = tokenFixture.createAccessToken(user);

			// when & then
			mockMvc.perform(
					get("/api/v1/projects/check")
						.header("Authorization", "Bearer " + accessToken)
						.param("url", invalidUrl))
				.andExpect(status().isBadRequest());
		}
	}
}
