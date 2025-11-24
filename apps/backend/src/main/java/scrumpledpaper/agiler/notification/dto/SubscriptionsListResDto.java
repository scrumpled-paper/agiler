package scrumpledpaper.agiler.notification.dto;

import scrumpledpaper.agiler.notification.domain.NotificationSubscription;

import java.util.List;

public record SubscriptionsListResDto(
		List<NotificationSubscription> subscriptions
) {
}
