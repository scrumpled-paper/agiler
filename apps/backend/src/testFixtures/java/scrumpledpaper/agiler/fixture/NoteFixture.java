package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.note.entity.Meeting;
import scrumpledpaper.agiler.note.entity.Retro;
import scrumpledpaper.agiler.note.entity.Scrum;
import scrumpledpaper.agiler.project.entity.Project;

public class NoteFixture {

	public static Retro createRetro(Project project, String title, String contents) {
		return Retro.builder()
			.project(project)
			.title(title)
			.contents(contents)
			.build();
	}

	public static Scrum createScrum(Project project, String title, String contents) {
		return Scrum.builder()
			.project(project)
			.title(title)
			.contents(contents)
			.build();
	}

	public static Meeting createMeeting(Project project, String title, String contents) {
		return Meeting.builder()
			.project(project)
			.title(title)
			.contents(contents)
			.build();
	}
}