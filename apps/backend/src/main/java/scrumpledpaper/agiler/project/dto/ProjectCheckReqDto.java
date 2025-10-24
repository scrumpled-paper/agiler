package scrumpledpaper.agiler.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProjectCheckReqDto (
	@NotBlank(message = "프로젝트 URL은 필수입니다.")
	@Size(max = 40, message = "프로젝트 URL은 최대 40자까지 가능합니다.")
	@Pattern(
		regexp = "^[a-zA-Z0-9-]+_[a-zA-Z0-9-]+$",
		message = "올바른 URL 형식이 아닙니다."
	)
	String url
) {}
