package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.template.entity.RetroTemplate;

public class RetroTemplateFixture {
	public static RetroTemplate createRetroTemplate(Project project, String title, String description, String contents) {
		return RetroTemplate.builder()
			.project(project)
			.title(title)
			.description(description)
			.contents(contents)
			.build();
	}
}

