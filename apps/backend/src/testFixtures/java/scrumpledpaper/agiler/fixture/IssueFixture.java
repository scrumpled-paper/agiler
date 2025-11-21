package scrumpledpaper.agiler.fixture;

import static scrumpledpaper.agiler.common.TestDataFactory.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import scrumpledpaper.agiler.kanban.dto.IssueCreateReqDto;
import scrumpledpaper.agiler.kanban.entity.Issue;
import scrumpledpaper.agiler.kanban.entity.IssueLabel;
import scrumpledpaper.agiler.kanban.entity.IssueProfile;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.kanban.entity.Label;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;

public class IssueFixture {

	public static Issue createIssue(Project project, KanbanConfig kanbanConfig, Boolean isDone, LocalDateTime startedAt, LocalDateTime dueAt) {
		return Issue.builder()
			.project(project)
			.kanbanConfig(kanbanConfig)
			.title(randomString(10))
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

	public static List<IssueProfile> createIssueProfiles(Issue issue, List<Profile> assignees) {
		return assignees.stream()
			.map(profile -> new IssueProfile(null, issue, profile))
			.collect(Collectors.toList());
	}

	public static List<IssueLabel> createIssueLabels(Issue issue, List<Label> labels) {
		return labels.stream()
			.map(label -> new IssueLabel(null, issue, label))
			.collect(Collectors.toList());
	}
}
