package scrumpledpaper.agiler.kanban.dto;

import jakarta.validation.constraints.NotNull;

public record IssueKanbanConfigReqDto(
	@NotNull(message = "칸반 설정 ID는 필수입니다.")
	Long kanbanConfigId
) {}

