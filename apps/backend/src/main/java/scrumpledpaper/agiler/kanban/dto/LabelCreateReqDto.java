package scrumpledpaper.agiler.kanban.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LabelCreateReqDto(
	@NotBlank(message = "라벨 이름은 필수입니다.")
	@Size(max = 20, message = "라벨 이름은 20자 이하여야 합니다.")
	String name,

	@Size(max = 100, message = "설명은 100자 이하여야 합니다.")
	String description,

	@NotBlank(message = "색상 코드는 필수입니다")
	@Pattern(
		regexp = "^#[A-Fa-f0-9]{6}$",
		message = "색상 코드는 #FFFFFF 형식이어야 합니다. (예: #FF0000, #00FF00)"
	)
	String color
) {}
