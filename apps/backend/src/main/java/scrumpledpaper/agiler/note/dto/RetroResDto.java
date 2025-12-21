package scrumpledpaper.agiler.note.dto;

import java.time.LocalDateTime;

public record RetroResDto(
	long retroId,
	String title,
	LocalDateTime createdAt,
	RetroResDto.ParticipantResDto[] participants
) {
	public record ParticipantResDto(
		long profileId,
		String nickname,
		String imageUrl
	) {}
}
