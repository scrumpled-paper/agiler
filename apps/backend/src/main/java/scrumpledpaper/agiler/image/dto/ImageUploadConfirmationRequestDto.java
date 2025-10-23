package scrumpledpaper.agiler.image.dto;

import jakarta.validation.constraints.NotBlank;

public record ImageUploadConfirmationRequestDto(
		@NotBlank
		String objectKey
) {
}
