package scrumpledpaper.agiler.kanban.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record IssueAssigneesReqDto (
	@NotNull(message = "담당자 목록은 필수입니다.")
	List<Long> assignees
) {}
