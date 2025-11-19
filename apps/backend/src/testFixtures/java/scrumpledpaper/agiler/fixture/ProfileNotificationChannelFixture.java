package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.notification.domain.ChannelType;
import scrumpledpaper.agiler.notification.domain.ProfileNotificationChannel;

public class ProfileNotificationChannelFixture {

	public static ProfileNotificationChannel create(long userId, long profileId, ChannelType channelType, String webhookUrl, String name) {
		return ProfileNotificationChannel.builder()
				.userId(userId)
				.profileId(profileId)
				.channelType(channelType)
				.webhookUrl(webhookUrl)
				.name(name)
				.build();
	}

}
