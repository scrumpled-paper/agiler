package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.template.entity.ScrumTemplate;

public class ScrumTemplateFixture {
	public static ScrumTemplate createScrumTemplate(Project project, String title, String description, String contents) {
		return ScrumTemplate.builder()
			.project(project)
			.title(title)
			.description(description)
			.contents(contents)
			.build();
	}
}

