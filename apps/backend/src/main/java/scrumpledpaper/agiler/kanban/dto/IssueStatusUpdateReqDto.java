package scrumpledpaper.agiler.kanban.dto;

import jakarta.validation.constraints.NotNull;

public record IssueStatusUpdateReqDto(
		@NotNull
		Long fromKanbanConfigId,
		@NotNull
		Long toKanbanConfigId
) {
}
