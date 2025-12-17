package scrumpledpaper.agiler.kanban.dto;

import java.time.LocalDateTime;
import java.util.List;

public record KanbanBoardResDto(
	List<KanbanConfigDto> kanbanConfigs,
	List<ProfileDto> profiles,
	List<LabelDto> labels,
	List<IssueDto> issues
	) {

	public record KanbanConfigDto(
		Long kanbanConfigId,
		String statusName,
		Integer priority,
		Boolean isDefault,
		Boolean backlog,
		Boolean isDone
	) {}

	public record ProfileDto(
		Long profileId,
		String nickname,
		String email,
		String imageUrl,
		String role,
		String description
	) {}

	public record LabelDto(
		Long labelId,
		String name,
		String color,
		String description
	) {}

	public record IssueDto(
		Long issueId,
		String title,
		Long kanbanConfigId,
		Boolean isDone,
		LocalDateTime createdAt,
		List<Long> assignees,
		List<Long> labels,
		List<IssueNoti> notis,
		LocalDateTime startedAt,
		LocalDateTime dueAt
	) {}

	public record IssueNoti(
		Long notiId,
		Long profileId
	) {}
}


