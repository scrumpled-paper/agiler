package scrumpledpaper.agiler.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DiscordOAuthResponseDto {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("webhook")
    private Webhook webhook;

    @Getter
    @Setter
    @ToString
    public static class Webhook {
        private String id;
        private String name;
        private String url;
        @JsonProperty("channel_id")
        private String channelId;
    }
}
