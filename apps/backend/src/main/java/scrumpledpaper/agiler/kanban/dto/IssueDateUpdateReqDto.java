package scrumpledpaper.agiler.kanban.dto;

import java.time.LocalDateTime;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;

public record IssueDateUpdateReqDto(
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	Optional<LocalDateTime> startedAt,
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	Optional<LocalDateTime> dueAt
) {}
