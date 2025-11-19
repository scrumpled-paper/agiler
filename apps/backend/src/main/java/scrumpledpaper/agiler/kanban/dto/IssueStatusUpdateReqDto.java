package scrumpledpaper.agiler.kanban.dto;

import jakarta.validation.constraints.NotBlank;

public record IssueStatusUpdateReqDto(
		@NotBlank
		String oldStatus,
		@NotBlank
		String newStatus
) {
}
