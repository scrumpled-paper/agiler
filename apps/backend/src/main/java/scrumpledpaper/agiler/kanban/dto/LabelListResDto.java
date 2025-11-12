package scrumpledpaper.agiler.kanban.dto;

import java.util.List;

public record LabelListResDto(
	List<LabelResDto> labels,
	int size
) {}
