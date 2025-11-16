package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.kanban.entity.Label;
import scrumpledpaper.agiler.project.entity.Project;

public class LabelFixture {
	public static Label createLabel(Project project, String name, String color, String description) {
		return Label.builder()
			.project(project)
			.name(name)
			.color(color)
			.description(description)
			.build();
	}
}
