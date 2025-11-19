package scrumpledpaper.agiler.notification.dto;

import scrumpledpaper.agiler.notification.domain.ChannelType;

public record ProfileNotificationChannelReqDto(
		ChannelType channelType,
		String webhookUrl,
		String name
) {
}
