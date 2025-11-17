package scrumpledpaper.agiler.template.dto;

import jakarta.validation.constraints.NotNull;

public record ScrumTemplateDeleteReqDto(
	@NotNull
	long templateId
) {}

