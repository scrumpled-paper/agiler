package scrumpledpaper.agiler.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateImageReqDto(
		@NotBlank
		String objectKey
) {
}
