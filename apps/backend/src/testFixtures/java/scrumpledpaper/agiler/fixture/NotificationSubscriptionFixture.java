package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.notification.domain.NotificationSubscription;

public class NotificationSubscriptionFixture {

	public static NotificationSubscription create(long userId, long profileId, long issueId, String fromStatus, String toStatus) {
		return NotificationSubscription.builder()
				.userId(userId)
				.profileId(profileId)
				.issueId(issueId)
				.fromStatus(fromStatus)
				.toStatus(toStatus)
				.build();
	}

}
