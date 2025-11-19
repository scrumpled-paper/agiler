package scrumpledpaper.agiler.notification.dto;

import scrumpledpaper.agiler.notification.domain.ProfileNotificationChannel;

import java.util.List;

public record NotificationChannelsResDto(
		List<ProfileNotificationChannelDto> channels
) {

	public static NotificationChannelsResDto from(
			List<ProfileNotificationChannel> entities
	) {
		List<ProfileNotificationChannelDto> channelDtos = entities.stream()
				.map(ProfileNotificationChannelDto::from)
				.toList();

		return new NotificationChannelsResDto(channelDtos);
	}

	public record ProfileNotificationChannelDto(
			long id,
			String channelType
	) {
		public static ProfileNotificationChannelDto from(
				ProfileNotificationChannel entity
		) {
			return new ProfileNotificationChannelDto(
					entity.getId(),
					entity.getChannelType().name()
			);
		}
	}

}
