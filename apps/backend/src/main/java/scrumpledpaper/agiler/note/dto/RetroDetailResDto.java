package scrumpledpaper.agiler.note.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RetroDetailResDto(
	long retroId,
	String title,
	String contents,
	LocalDateTime createdAt,
	List<ParticipantResDto> participants
) {
		public record ParticipantResDto(
			long profileId,
			String nickname,
			String imageUrl
		) {}
	}
