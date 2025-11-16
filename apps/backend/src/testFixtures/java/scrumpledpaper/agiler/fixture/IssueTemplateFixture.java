package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.template.entity.IssueTemplate;

public class IssueTemplateFixture {
	public static IssueTemplate createIssueTemplate(Project project, String title, String description, String contents) {
		return IssueTemplate.builder()
			.project(project)
			.title(title)
			.description(description)
			.contents(contents)
			.build();
	}
}
