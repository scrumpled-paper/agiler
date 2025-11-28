package scrumpledpaper.agiler.kanban.dto;

import java.util.List;

public record KanbanConfigListResDto(
	List<KanbanConfigResDto> contents,
	int size
) {}
