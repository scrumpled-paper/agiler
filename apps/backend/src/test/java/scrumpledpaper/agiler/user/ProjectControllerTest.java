package scrumpledpaper.agiler.user;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import scrumpledpaper.agiler.TestcontainersConfiguration;
import scrumpledpaper.agiler.fixture.ImageFixture;
import scrumpledpaper.agiler.fixture.ProjectFixture;
import scrumpledpaper.agiler.fixture.TokenFixture;
import scrumpledpaper.agiler.fixture.UserFixture;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.image.repository.ImageRepository;
import scrumpledpaper.agiler.project.dto.ProjectCreateReqDto;
import scrumpledpaper.agiler.project.dto.ProjectCreateResDto;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.repository.ProjectRepository;
import scrumpledpaper.agiler.user.entity.Profile;
import scrumpledpaper.agiler.user.entity.Role;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.repository.ProfileRepository;
import scrumpledpaper.agiler.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
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
			assertThat(createdProject.getTag()).isEqualTo(createReqDto.tag());
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
			assertThat(res).contains("U001").contains("사용자를 찾을 수 없습니다");
		}
	}
}
