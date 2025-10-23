package scrumpledpaper.agiler.image.dto;

import jakarta.validation.constraints.NotBlank;

public record PreSignedUrlRequestDto(
		@NotBlank
		String fileName,
		@NotBlank
		String contentType
) {
}
