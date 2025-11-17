package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.template.entity.MeetingTemplate;

public class MeetingTemplateFixture {
	public static MeetingTemplate createMeetingTemplate(Project project, String title, String description, String contents) {
		return MeetingTemplate.builder()
			.project(project)
			.title(title)
			.description(description)
			.contents(contents)
			.build();
	}
}

