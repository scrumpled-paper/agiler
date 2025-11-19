package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.notification.domain.ScheduledNotification;

import java.time.LocalDateTime;

public class ScheduleNotificationFixture {

	public static ScheduledNotification create(long userId, long profileId, long issueId, String message, LocalDateTime scheduledDateTime) {
		return ScheduledNotification.builder()
				.userId(userId)
				.profileId(profileId)
				.issueId(issueId)
				.notificationTime(scheduledDateTime)
				.message(message)
				.build();
	}

}
