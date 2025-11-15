package scrumpledpaper.agiler.template.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IssueTemplateCreateReqDto(
	@NotBlank(message = "템플릿 제목은 필수입니다.")
	@Size(max = 20, message = "템플릿 제목은 50자 이하여야 합니다.")
	String title,

	@Size(max = 100, message = "설명은 100자 이하여야 합니다.")
	String description,

	String contents
) {}
