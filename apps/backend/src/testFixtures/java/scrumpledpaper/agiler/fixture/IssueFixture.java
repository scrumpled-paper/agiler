package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.kanban.entity.Issue;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.project.entity.Profile;

import java.time.LocalDateTime;

public class IssueFixture {

	public static Issue createIssue(KanbanConfig kanbanConfig, Profile profile, String title, Boolean isDone, String contents, LocalDateTime startedAt, LocalDateTime dueAt) {
		return Issue.builder()
				.kanbanConfig(kanbanConfig)
				.profile(profile)
				.title(title)
				.isDone(isDone)
				.contents(contents)
				.startedAt(startedAt)
				.dueAt(dueAt)
				.build();
	}

}
