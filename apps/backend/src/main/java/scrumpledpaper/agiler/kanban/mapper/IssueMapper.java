package scrumpledpaper.agiler.kanban.mapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import scrumpledpaper.agiler.kanban.dto.IssueCreateReqDto;
import scrumpledpaper.agiler.kanban.dto.IssueDetailResDto;
import scrumpledpaper.agiler.kanban.dto.KanbanBoardResDto;
import scrumpledpaper.agiler.kanban.entity.Issue;
import scrumpledpaper.agiler.kanban.entity.IssueLabel;
import scrumpledpaper.agiler.kanban.entity.IssueProfile;
import scrumpledpaper.agiler.kanban.entity.IssueSnapshotDateMapping;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.kanban.entity.Label;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;

@Mapper(componentModel = "spring")
public interface IssueMapper {
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "project", source = "project")
	@Mapping(target = "kanbanConfig", source = "kanbanConfig")
	@Mapping(target = "labels", ignore = true)
	@Mapping(target = "assignees", ignore = true)
	@Mapping(target = "title", source = "issueCreateReqDto.title")
	@Mapping(target = "contents", source = "issueCreateReqDto.contents")
	@Mapping(target = "isDone", defaultValue = "false")
	Issue toEntity(Project project, KanbanConfig kanbanConfig, IssueCreateReqDto issueCreateReqDto);

	default List<IssueLabel> toIssueLabel(Issue issue, List<Label> labels) {
		if (labels.isEmpty()) {
			return new ArrayList<>();
		}

		return labels.stream()
			.map(label -> IssueLabel.builder()
				.issue(issue)
				.label(label)
				.build())
			.collect(Collectors.toList());
	}

	default List<IssueProfile> toIssueProfile(Issue issue, List<Profile> assignees) {
		if (assignees.isEmpty()) {
			return new ArrayList<>();
		}

		return assignees.stream()
			.map(assignee -> IssueProfile.builder()
				.issue(issue)
				.profile(assignee)
				.build())
			.collect(Collectors.toList());
	}

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "kanbanConfig", source = "kanbanConfig")
	@Mapping(target = "project", source = "project")
	@Mapping(target = "title", source = "issue.title")
	@Mapping(target = "isDone", source = "issue.isDone")
	Issue toEntity(Project project, Issue issue, KanbanConfig kanbanConfig);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "snapshotDate", source = "snapshotDate")
	@Mapping(target = "project", source = "project")
	IssueSnapshotDateMapping toIssueSnapshotDateMapping(Project project, LocalDate snapshotDate, int issueCount);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "issue", source = "issue")
	IssueLabel toIssueLabel(Issue issue, IssueLabel issueLabel);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "issue", source = "issue")
	IssueProfile toIssueProfile(Issue issue, IssueProfile issueProfile);

	default KanbanBoardResDto.IssueDto toKanbanBoardIssueDto(Issue issue, List<KanbanBoardResDto.IssueNoti> notis) {
		return new KanbanBoardResDto.IssueDto(
			issue.getId(),
			issue.getTitle(),
			issue.getKanbanConfig().getId(),
			issue.getIsDone(),
			issue.getCreatedAt(),
			issue.getAssignees().stream()
				.map(a -> a.getProfile().getId())
				.toList(),
			issue.getLabels().stream()
				.map(l -> l.getLabel().getId())
				.toList(),
			notis,
			issue.getStartedAt(),
			issue.getDueAt()
		);
	}

	@Mapping(target = "issueId", source = "issue.id")
	@Mapping(target = "isDone", source = "issue.isDone")
	@Mapping(target = "createdAt", source = "issue.createdAt")
	@Mapping(target = "kanbanConfig.kanbanConfigId", source = "kanbanConfig.id")
	@Mapping(target = "kanbanConfig.statusName", source = "kanbanConfig.statusName")
	@Mapping(target = "kanbanConfig.priority", source = "kanbanConfig.priority")
	@Mapping(target = "kanbanConfig.isDefault", source = "kanbanConfig.defaultStatus")
	@Mapping(target = "kanbanConfig.backlog", source = "kanbanConfig.backlog")
	@Mapping(target = "kanbanConfig.isDone", source = "kanbanConfig.isDone")
	IssueDetailResDto toIssueDetailDto(Issue issue, List<IssueDetailResDto.LabelDto> labels, List<IssueDetailResDto.AssigneeDto> assignees, KanbanConfig kanbanConfig);
}
