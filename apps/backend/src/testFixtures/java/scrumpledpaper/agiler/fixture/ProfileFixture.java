package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.user.entity.Profile;
import scrumpledpaper.agiler.user.entity.Role;
import scrumpledpaper.agiler.user.entity.User;

public class ProfileFixture {

	public static Profile createProfile(Role role, User user, Project project) {
		return Profile.builder()
			.role(role)
			.user(user)
			.project(project)
			.build();
	}
}
