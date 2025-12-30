package scrumpledpaper.agiler.note.dto.internal;

public record NoteDataResDto(
	Long id,
	String type,
	String title,
	String contents
) {
}
