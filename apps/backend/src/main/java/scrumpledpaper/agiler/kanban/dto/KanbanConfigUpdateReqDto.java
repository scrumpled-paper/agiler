package scrumpledpaper.agiler.kanban.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record KanbanConfigUpdateReqDto(
	@NotNull(message = "칸반 설정 목록은 필수입니다.")
	List<KanbanConfigReqDto> kanbanConfigs
) {
	public record KanbanConfigReqDto(
		@NotNull(message = "상태 이름은 필수입니다.")
		@Size(min = 1, max = 20, message = "상태 이름은 1자 이상 20자 이하여야 합니다.")
		String statusName,
		@NotNull(message = "우선순위는 필수입니다.")
		@Size(min = 0, message = "우선순위는 0 이상이어야 합니다.")
		Integer priority,
		@NotNull(message = "기본 상태 여부는 필수입니다.")
		Boolean defaultStatus,
		@NotNull(message = "백로그 여부는 필수입니다.")
		Boolean backlog,
		@NotNull(message = "완료 상태 여부는 필수입니다.")
		Boolean isDone
	) {}
}

