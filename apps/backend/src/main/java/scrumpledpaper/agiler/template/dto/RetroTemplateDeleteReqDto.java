package scrumpledpaper.agiler.template.dto;

import jakarta.validation.constraints.NotNull;

public record RetroTemplateDeleteReqDto(
	@NotNull
	long templateId
) {}

