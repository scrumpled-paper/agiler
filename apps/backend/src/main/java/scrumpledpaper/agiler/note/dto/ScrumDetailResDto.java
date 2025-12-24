package scrumpledpaper.agiler.note.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ScrumDetailResDto(
	long scrumId,
	String title,
	String contents,
	LocalDateTime createdAt,
	List<ParticipantResDto> participants,
	List<IssueResDto> issues
) {
	public record ParticipantResDto(
		long profileId,
		String nickname,
		String imageUrl
	) {}

	public record IssueResDto(
		long issueId,
		String title,
		List<AssigneeDto> assignees
	) {}

	public record AssigneeDto(
		long profileId,
		String nickname,
		String imageUrl
	) {}
}
