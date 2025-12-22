package scrumpledpaper.agiler.note.dto;

import jakarta.validation.constraints.NotNull;

public record NoteDeleteReqDto(
	@NotNull
	Long id
) {}
