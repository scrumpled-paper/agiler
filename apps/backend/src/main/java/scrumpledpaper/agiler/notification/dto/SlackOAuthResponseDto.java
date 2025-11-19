package scrumpledpaper.agiler.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SlackOAuthResponseDto {

    private boolean ok;
    private String error;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("incoming_webhook")
    private IncomingWebhook incomingWebhook;

    @Getter
    @Setter
    @ToString
    public static class IncomingWebhook {
        private String channel;
        @JsonProperty("channel_id")
        private String channelId;
        private String url;
    }
}
