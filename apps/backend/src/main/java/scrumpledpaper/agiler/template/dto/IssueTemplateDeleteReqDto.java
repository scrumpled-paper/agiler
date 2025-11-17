package scrumpledpaper.agiler.template.dto;

import jakarta.validation.constraints.NotNull;

public record IssueTemplateDeleteReqDto(
	@NotNull
	long templateId
) {}
