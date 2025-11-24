package scrumpledpaper.agiler.notification.dto;

import jakarta.validation.constraints.NotNull;

public record NotificationSubscriptionReqDto(
		@NotNull
		Long issueId,
		@NotNull
		Long fromKanbanConfigId,
		@NotNull
		Long toKanbanConfigId
) {
}
