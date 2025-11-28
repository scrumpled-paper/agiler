package scrumpledpaper.agiler.kanban.dto;

public record KanbanConfigResDto(
	Long id,
	String statusName,
	int priority,
	boolean defaultStatus,
	boolean backlog,
	boolean isDone
) {}
