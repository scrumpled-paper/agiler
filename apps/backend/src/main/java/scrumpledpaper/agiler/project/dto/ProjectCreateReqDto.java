package scrumpledpaper.agiler.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectCreateReqDto (
	@NotBlank
	@Size(max = 20, message = "프로젝트 이름은 최대 20자까지 가능합니다.")
	String title,

	@NotBlank
	@Size(max = 20, message = "프로젝트 URL은 최대 20자까지 가능합니다.")
	String url,

	@NotBlank
	@Size(max = 20, message = "프로젝트 태그는 최대 20자까지 가능합니다.")
	String tag,

	@NotBlank
	String summary
) {}
