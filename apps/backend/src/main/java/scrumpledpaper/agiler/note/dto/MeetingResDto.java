package scrumpledpaper.agiler.note.dto;

import java.time.LocalDateTime;

public record MeetingResDto(
	long meetingId,
	String title,
	LocalDateTime createdAt,
	ParticipantResDto[] participants
) {
	public record ParticipantResDto(
		long profileId,
		String nickname,
		String imageUrl
	) {}
}
