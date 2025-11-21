package scrumpledpaper.agiler.fixture;

import static scrumpledpaper.agiler.common.TestDataFactory.*;

import java.time.LocalDateTime;
import java.util.List;

import scrumpledpaper.agiler.kanban.dto.IssueCreateReqDto;
import scrumpledpaper.agiler.kanban.entity.Issue;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;

public class IssueFixture {

	public static Issue createIssue(Project project, KanbanConfig kanbanConfig, Boolean isDone, LocalDateTime startedAt, LocalDateTime dueAt) {
		return Issue.builder()
			.project(project)
			.kanbanConfig(kanbanConfig)
			.isDone(isDone)
			.startedAt(startedAt)
			.dueAt(dueAt)
			.build();
	}

	public static IssueCreateReqDto createIssueCreateReqDto(List<Long> assignees, List<Long> labels, LocalDateTime startedAt, LocalDateTime dueAt) {
		return new IssueCreateReqDto(
			randomString(10),
			randomString(50),
			assignees,
			labels,
			startedAt,
			dueAt
		);
	}
}
