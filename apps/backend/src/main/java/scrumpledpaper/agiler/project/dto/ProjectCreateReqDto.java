package scrumpledpaper.agiler.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProjectCreateReqDto (
	@NotBlank(message = "프로젝트 이름은 필수입니다.")
	@Size(max = 20, message = "프로젝트 이름은 최대 20자까지 가능합니다.")
	String title,

	@NotBlank(message = "프로젝트 URL은 필수입니다.")
	@Size(max = 40, message = "프로젝트 URL은 최대 40자까지 가능합니다.")
	@Pattern(
		regexp = "^[a-z0-9-]+$",
		message = "프로젝트 URL은 소문자, 숫자, 하이픈만 사용할 수 있습니다."
	)
	String url,
	String summary
) {}
