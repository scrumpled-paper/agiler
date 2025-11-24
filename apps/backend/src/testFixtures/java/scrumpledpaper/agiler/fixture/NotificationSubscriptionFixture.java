package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.notification.domain.NotificationSubscription;

public class NotificationSubscriptionFixture {

	public static NotificationSubscription create(long userId, long profileId, long issueId, long fromKanbanConfigId, long toKanbanConfigId) {
		return NotificationSubscription.builder()
				.userId(userId)
				.profileId(profileId)
				.issueId(issueId)
				.fromKanbanConfigId(fromKanbanConfigId)
				.toKanbanConfigId(toKanbanConfigId)
				.build();
	}

}
