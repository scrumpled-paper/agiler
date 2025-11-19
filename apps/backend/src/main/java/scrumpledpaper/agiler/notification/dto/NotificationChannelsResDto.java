package scrumpledpaper.agiler.notification.dto;

import java.util.List;

public record NotificationChannelsResDto(
		List<ProfileNotificationChannel> channels
) {

	public static NotificationChannelsResDto from(
			List<scrumpledpaper.agiler.notification.domain.ProfileNotificationChannel> entities
	) {
		var channelDtos = entities.stream()
				.map(ProfileNotificationChannel::from)
				.toList();

		return new NotificationChannelsResDto(channelDtos);
	}

	public record ProfileNotificationChannel(
			long id,
			String channelType
	) {
		public static ProfileNotificationChannel from(
				scrumpledpaper.agiler.notification.domain.ProfileNotificationChannel entity
		) {
			return new ProfileNotificationChannel(
					entity.getId(),
					entity.getChannelType().name()
			);
		}
	}

}
