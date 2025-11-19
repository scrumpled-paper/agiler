package scrumpledpaper.agiler.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import scrumpledpaper.agiler.notification.dto.DiscordOAuthResDto;

import java.util.Map;

@FeignClient(name = "discordAuthClient", url = "https://discord.com/api")
public interface DiscordAuthClient {

    @PostMapping(value = "/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	DiscordOAuthResDto getAccessToken(Map<String, ?> form);
}
