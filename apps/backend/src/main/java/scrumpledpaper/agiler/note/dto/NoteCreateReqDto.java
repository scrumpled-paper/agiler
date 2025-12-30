package scrumpledpaper.agiler.note.dto;

import jakarta.validation.constraints.NotBlank;

public record NoteCreateReqDto(
		@NotBlank
		String type
) {
}
