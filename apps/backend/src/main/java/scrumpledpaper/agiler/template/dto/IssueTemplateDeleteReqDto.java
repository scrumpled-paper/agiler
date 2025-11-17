package scrumpledpaper.agiler.template.dto;

import jakarta.validation.constraints.NotNull;

public record IssueTemplateDeleteReqDto(
	@NotNull(message = "템플릿 ID는 필수입니다.")
	Long templateId
) {}
