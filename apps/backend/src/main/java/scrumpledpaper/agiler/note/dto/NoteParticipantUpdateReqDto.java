package scrumpledpaper.agiler.note.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record NoteParticipantUpdateReqDto(
	@NotNull(message = "참여자 ID 목록은 필수입니다.")
	List<Long> participantIds
) {}
