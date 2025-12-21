package scrumpledpaper.agiler.fixture;

import java.util.List;

import scrumpledpaper.agiler.common.TestDataFactory;
import scrumpledpaper.agiler.note.entity.Scrum;
import scrumpledpaper.agiler.note.entity.ScrumProfile;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;

public class ScrumFixture {
	public static Scrum createScrum(Project project) {
		return Scrum.builder()
			.title(TestDataFactory.randomString(10))
			.contents(TestDataFactory.randomString(50))
			.project(project)
			.build();
	}

	public static List<ScrumProfile> createScrumProfiles(Scrum scrum, List<Profile> participants) {
		return participants.stream()
			.map(participant -> ScrumProfile.builder()
				.scrum(scrum)
				.profile(participant)
				.build())
			.toList();
	}
}
