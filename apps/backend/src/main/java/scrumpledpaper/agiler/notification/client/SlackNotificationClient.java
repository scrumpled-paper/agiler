package scrumpledpaper.agiler.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;

@FeignClient(name = "slackNotificationClient", url = "http://dummyurl") // The URL is dynamic and will be overridden in the method call
public interface SlackNotificationClient {
    @PostMapping
    void sendNotification(URI webhookUrl, @RequestBody String payload);
}
