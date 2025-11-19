package scrumpledpaper.agiler.notification.dto;

public record NotificationSubscriptionRequestDto(
		Long issueId,
		String fromStatus,
		String toStatus
) {
}
