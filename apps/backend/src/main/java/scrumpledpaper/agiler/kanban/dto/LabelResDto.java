package scrumpledpaper.agiler.kanban.dto;

public record LabelResDto(
	long id,
	String name,
	String description,
	String color
) {}
