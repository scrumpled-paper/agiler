package scrumpledpaper.agiler.notification.dto;

import jakarta.validation.constraints.NotNull;

public record NotificationSubscriptionRequestDto(
		@NotNull
		Long issueId,
		@NotNull
		Long fromKanbanConfigId,
		@NotNull
		Long toKanbanConfigId
) {
}
