package scrumpledpaper.agiler.note.dto;

import java.util.List;

import scrumpledpaper.agiler.note.entity.NoteType;

public record NoteParticipantResDto(
	long noteId,
	NoteType noteType,
	List<ParticipantResDto> participants
) {
	public record ParticipantResDto(
		long profileId,
		String nickname,
		String imageUrl
	) {}
}
