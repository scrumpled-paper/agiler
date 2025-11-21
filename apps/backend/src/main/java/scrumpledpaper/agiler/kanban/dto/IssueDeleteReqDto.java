package scrumpledpaper.agiler.kanban.dto;

import jakarta.validation.constraints.NotNull;

public record IssueDeleteReqDto(
	@NotNull(message = "이슈 ID는 필수입니다.")
	Long issueId
) {}
