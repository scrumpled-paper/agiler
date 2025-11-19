package scrumpledpaper.agiler.notification.dto;

import scrumpledpaper.agiler.notification.domain.ChannelType;

public record ProfileNotificationChannelRequestDto(
		ChannelType channelType,
		String webhookUrl,
		String name
) {
}
