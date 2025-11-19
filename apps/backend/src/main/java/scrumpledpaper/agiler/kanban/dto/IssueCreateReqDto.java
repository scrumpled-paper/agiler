package scrumpledpaper.agiler.kanban.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IssueCreateReqDto(
	@NotBlank(message = "이슈 이름은 필수입니다.")
	@Size(max = 20, message = "이슈 이름은 20자 이하여야 합니다.")
	String title,
	String contents,
	Long assigneeId,
	@NotNull(message = "라벨 목록은 필수입니다.")
	List<Long> labels,
	LocalDateTime startedAt,
	LocalDateTime dueAt
) {}
