package scrumpledpaper.agiler.note.dto;

import java.time.LocalDateTime;

public record ScrumResDto(
	long scrumId,
	String title,
	LocalDateTime createdAt,
	ScrumResDto.ParticipantResDto[] participants
) {
	public record ParticipantResDto(
		long profileId,
		String nickname,
		String imageUrl
	) {}
}
