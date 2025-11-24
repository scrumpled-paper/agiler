package scrumpledpaper.agiler.kanban.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import scrumpledpaper.agiler.kanban.dto.IssueCreateReqDto;
import scrumpledpaper.agiler.kanban.entity.Issue;
import scrumpledpaper.agiler.kanban.entity.IssueLabel;
import scrumpledpaper.agiler.kanban.entity.IssueProfile;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.kanban.entity.Label;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;

@Mapper(componentModel = "spring")
public interface IssueMapper {
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "project", source = "project")
	@Mapping(target = "kanbanConfig", source = "kanbanConfig")
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
}
