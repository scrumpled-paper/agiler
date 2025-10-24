package scrumpledpaper.agiler.common;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.fixture.ImageFixture;
import scrumpledpaper.agiler.fixture.ProfileFixture;
import scrumpledpaper.agiler.fixture.ProjectFixture;
import scrumpledpaper.agiler.fixture.TokenFixture;
import scrumpledpaper.agiler.fixture.UserFixture;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.image.repository.ImageRepository;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.repository.ProjectRepository;
import scrumpledpaper.agiler.user.entity.Profile;
import scrumpledpaper.agiler.user.entity.Role;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.repository.ProfileRepository;
import scrumpledpaper.agiler.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class TestDataFactory {
	private final TokenFixture tokenFixture;
	private final UserRepository userRepository;
	private final ImageRepository imageRepository;
	private final ProfileRepository profileRepository;
	private final ProjectRepository projectRepository;
	private final EntityManager entityManager;

	public Image createDefaultImage() {
		Image image = ImageFixture.createImage();
		return imageRepository.save(image);
	}

	public String createNotAllowedAccessToken() {
		return tokenFixture.createNotAllowedAccessToken();
	}

	public String createAccessToken(User user) {
		return tokenFixture.createAccessToken(user);
	}

	public AuthContext createAuth(Image defaultImage) {
		User user = UserFixture.createUser(defaultImage);
		userRepository.save(user);
		String token = tokenFixture.createAccessToken(user);
		return new AuthContext(user, token);
	}

	public User createUser(long imageId) {
		User user = UserFixture.createUser(imageId);
		return userRepository.save(user);
	}

	public Project createProject(String url) {
		Project project = ProjectFixture.createProject(url);
		return projectRepository.save(project);
	}

	public Project createProjectAndOwnerProfile(String url, User user) {
		Project project = ProjectFixture.createProject(url);
		projectRepository.save(project);
		Profile profile = ProfileFixture.createProfile(Role.owner, user, project);
		profileRepository.save(profile);
		return project;
	}
