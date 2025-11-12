package scrumpledpaper.agiler.kanban.dto;

public record LabelResDto(
	Long id,
	String name,
	String description,
	String color
) {}
