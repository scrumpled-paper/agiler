package scrumpledpaper.agiler.kanban.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record IssueLabelsReqDto(
	@NotNull(message = "라벨 목록은 필수입니다.")
	List<Long> labels
) {}

