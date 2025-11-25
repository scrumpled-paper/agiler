package scrumpledpaper.agiler.project.dto;

import jakarta.validation.constraints.NotBlank;

public record ImageUpdateReqDto(
		@NotBlank
		String objectKey
) {
}
