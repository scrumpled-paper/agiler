package scrumpledpaper.agiler.kanban.dto;

import jakarta.validation.constraints.NotNull;

public record LabelDeleteReqDto(
	@NotNull(message = "템플릿 ID는 필수입니다.")
	Long templateId
) {}
