package scrumpledpaper.agiler.fixture;

import java.util.List;

import scrumpledpaper.agiler.common.TestDataFactory;
import scrumpledpaper.agiler.note.entity.Meeting;
import scrumpledpaper.agiler.note.entity.MeetingProfile;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;

public class MeetingFixture {
	public static Meeting createMeeting(Project project) {
		return Meeting.builder()
			.title(TestDataFactory.randomString(10))
			.contents(TestDataFactory.randomString(50))
			.project(project)
			.build();
	}

	public static List<MeetingProfile> createMeetingProfiles(Meeting savedMeeting, List<Profile> participants) {
		return participants.stream()
			.map(participant -> MeetingProfile.builder()
				.meeting(savedMeeting)
				.profile(participant)
				.build())
			.toList();
	}
}
