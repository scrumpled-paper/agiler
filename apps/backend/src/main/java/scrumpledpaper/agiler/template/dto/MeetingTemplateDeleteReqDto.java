package scrumpledpaper.agiler.template.dto;

import jakarta.validation.constraints.NotNull;

public record MeetingTemplateDeleteReqDto(
	@NotNull
	long templateId
) {}

