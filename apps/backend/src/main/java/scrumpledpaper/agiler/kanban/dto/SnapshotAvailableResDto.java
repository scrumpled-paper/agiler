package scrumpledpaper.agiler.kanban.dto;

import java.time.LocalDate;
import java.util.List;

public record SnapshotAvailableResDto (
	List<LocalDate> availableDates
) {}
