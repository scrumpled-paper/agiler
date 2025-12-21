package scrumpledpaper.agiler.fixture;

import java.util.List;

import scrumpledpaper.agiler.common.TestDataFactory;
import scrumpledpaper.agiler.note.entity.Retro;
import scrumpledpaper.agiler.note.entity.RetroProfile;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;

public class RetroFixture {
	public static Retro createRetro(Project project) {
		return Retro.builder()
			.title(TestDataFactory.randomString(10))
			.contents(TestDataFactory.randomString(50))
			.project(project)
			.build();
	}

	public static List<RetroProfile> createRetroProfiles(Retro savedRetro, List<Profile> participants) {
		return participants.stream()
			.map(participant -> RetroProfile.builder()
				.retro(savedRetro)
				.profile(participant)
				.build())
			.toList();
	}
}
