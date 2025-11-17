package scrumpledpaper.agiler.kanban.dto;

import jakarta.validation.constraints.NotNull;

public record LabelDeleteReqDto(
	@NotNull
	long id
) {}
