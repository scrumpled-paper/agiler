package scrumpledpaper.agiler.notification.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import scrumpledpaper.agiler.notification.client.SlackNotificationClient;
import scrumpledpaper.agiler.notification.domain.ChannelType;

import java.net.URI;

@Slf4j
@Component("SLACK")
@RequiredArgsConstructor
public class SlackNotificationSender implements NotificationSender {

    private final SlackNotificationClient slackNotificationClient;

    @Override
    public boolean supports(String channelType) {
        return ChannelType.SLACK.name().equalsIgnoreCase(channelType);
    }

    @Override
    public void send(String webhookUrl, String message) {
        try {
            String payload = String.format("{\"text\": \"%s\"}", message);
            slackNotificationClient.sendNotification(URI.create(webhookUrl), payload);
            log.info("Slack notification sent successfully to {}", webhookUrl);
        } catch (Exception e) {
            log.error("Error sending Slack notification to {}: {}", webhookUrl, e.getMessage(), e);
        }
    }
}
