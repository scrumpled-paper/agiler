package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.notification.domain.ChannelType;
import scrumpledpaper.agiler.notification.domain.ProfileNotificationChannel;

public class ProfileNotificationChannelFixture {

	public static ProfileNotificationChannel create(long userId, long profileId, ChannelType channelType, String webhookUrl) {
		return ProfileNotificationChannel.builder()
				.userId(userId)
				.profileId(profileId)
				.channelType(channelType)
				.webhookUrl(webhookUrl)
				.build();
	}

}
