package scrumpledpaper.agiler.notification.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import scrumpledpaper.agiler.notification.client.DiscordNotificationClient;
import scrumpledpaper.agiler.notification.domain.ChannelType;

import java.net.URI;

@Slf4j
@Component("DISCORD")
@RequiredArgsConstructor
public class DiscordNotificationSender implements NotificationSender {

    private final DiscordNotificationClient discordNotificationClient;

    @Override
    public boolean supports(String channelType) {
        return ChannelType.DISCORD.name().equalsIgnoreCase(channelType);
    }

    @Override
    public void send(String webhookUrl, String message) {
        try {
            String payload = String.format("{\"content\": \"%s\"}", message);
            discordNotificationClient.sendNotification(URI.create(webhookUrl), payload);
            log.info("Discord notification sent successfully to {}", webhookUrl);
        } catch (Exception e) {
            log.error("Error sending Discord notification to {}: {}", webhookUrl, e.getMessage(), e);
        }
    }
}
