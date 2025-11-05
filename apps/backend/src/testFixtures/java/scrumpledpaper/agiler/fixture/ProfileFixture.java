package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.user.entity.Profile;
import scrumpledpaper.agiler.user.entity.Role;
import scrumpledpaper.agiler.user.entity.User;

public class ProfileFixture {

	public static Profile createProfile(User user, Project project, Role role) {
		return Profile.builder()
			.role(role)
			.user(user)
			.project(project)
			.nickname(user.getNickname())
			.imageId(user.getImageId())
			.email(user.getEmail())
			.description("example description")
			.build();
	}
}
