package scrumpledpaper.agiler.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import scrumpledpaper.agiler.notification.dto.SlackOAuthResponseDto;

import java.util.Map;

@FeignClient(name = "slackAuthClient", url = "https://slack.com/api")
public interface SlackAuthClient {

    @PostMapping(value = "/oauth.v2.access", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	SlackOAuthResponseDto getAccessToken(Map<String, ?> form);
}
