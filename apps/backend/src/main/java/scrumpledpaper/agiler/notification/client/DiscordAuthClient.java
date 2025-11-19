package scrumpledpaper.agiler.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import scrumpledpaper.agiler.notification.dto.DiscordOAuthResponseDto;

import java.util.Map;

@FeignClient(name = "discordAuthClient", url = "https://discord.com/api")
public interface DiscordAuthClient {

    @PostMapping(value = "/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	DiscordOAuthResponseDto getAccessToken(Map<String, ?> form);
}
