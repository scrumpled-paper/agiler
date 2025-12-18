package scrumpledpaper.agiler.kanban.dto;

import java.time.LocalDateTime;
import java.util.List;

public record IssueDetailResDto(
	long issueId,
	String title,
	KanbanConfigDto kanbanConfig,
	boolean isDone,
	String contents,
	LocalDateTime createdAt,
	List<AssigneeDto> assignees,
	List<LabelDto> labels,
	LocalDateTime startedAt,
	LocalDateTime dueAt
) {

	public record KanbanConfigDto(
		long kanbanConfigId,
		String statusName,
		Integer priority,
		Boolean isDefault,
		Boolean backlog,
		Boolean isDone
	) {}

	public record AssigneeDto(
		long profileId,
		String nickname,
		String email,
		String imageUrl,
		String role,
		String description
	) {}

	public record LabelDto(
		long labelId,
		String name,
		String color,
		String description
	) {}
}
