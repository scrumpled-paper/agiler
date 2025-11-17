package scrumpledpaper.agiler.template.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MeetingTemplateUpdateReqDto(
	@NotNull(message = "템플릿 ID는 필수입니다.")
	Long templateId,

	@NotBlank(message = "템플릿 제목은 필수입니다.")
	@Size(max = 20, message = "템플릿 제목은 20자 이하여야 합니다.")
	String title,

	@Size(max = 100, message = "설명은 100자 이하여야 합니다.")
	String description,

	String contents
) {}

