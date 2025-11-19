package scrumpledpaper.agiler.notification.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ScheduleNotificationRequestDto(
		@NotNull
		Long issueId,
		@NotNull @Min(value = 1, message = "Delay in minutes must be non-negative")
		Long delayInMinutes,

		String message
) {
}
