package scrumpledpaper.agiler.kanban.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IssueUpdateReqDto(
	@NotNull(message = "이슈 ID는 필수입니다.")
	long issueId,
	@NotBlank(message = "이슈 제목은 필수입니다.")
	@Size(max = 20, message = "이슈 제목은 20자 이하여야 합니다.")
	String title,
	@NotNull(message = "이슈 상태는 필수입니다.")
	long kanbanConfigId,
	String contents,
	LocalDateTime startedAt,
	LocalDateTime dueAt
) {}
