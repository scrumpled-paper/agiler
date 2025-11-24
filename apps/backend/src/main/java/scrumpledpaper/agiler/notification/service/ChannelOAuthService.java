package scrumpledpaper.agiler.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.notification.client.DiscordAuthClient;
import scrumpledpaper.agiler.notification.client.SlackAuthClient;
import scrumpledpaper.agiler.notification.domain.ChannelType;
import scrumpledpaper.agiler.notification.dto.DiscordOAuthResDto;
import scrumpledpaper.agiler.notification.dto.OAuthStatePayload;
import scrumpledpaper.agiler.notification.dto.ProfileNotificationChannelReqDto;
import scrumpledpaper.agiler.notification.dto.SlackOAuthResDto;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelOAuthService {

	private final DiscordAuthClient discordAuthClient;
	private final SlackAuthClient slackAuthClient;
	private final NotificationManagementService notificationManagementService;
	private final OAuthStateService oAuthStateService;

	@Value("${spring.security.oauth2.client.registration.slack.client-id}")
	private String slackClientId;

	@Value("${spring.security.oauth2.client.registration.slack.client-secret}")
	private String slackClientSecret;

	@Value("${spring.security.oauth2.client.registration.slack.redirect-uri}")
	private String slackRedirectUri;

	@Value("${spring.security.oauth2.client.registration.discord.client-id}")
	private String discordClientId;

	@Value("${spring.security.oauth2.client.registration.discord.client-secret}")
	private String discordClientSecret;

	@Value("${spring.security.oauth2.client.registration.discord.redirect-uri}")
	private String discordRedirectUri;

	private static final String SLACK_PREFIX = "Slack - ";
	private static final String DISCORD_PREFIX = "Discord - ";

	@Transactional
	public void processSlackCallback(String code, String state, long userId) {
		OAuthStatePayload payload = oAuthStateService.consumeState(state);

		if (payload.userId() != userId) {
			throw new CustomException(ErrorCode.SLACK_OAUTH_FAILED);
		}

		Map<String, String> formData = new HashMap<>();
		formData.put("client_id", slackClientId);
		formData.put("client_secret", slackClientSecret);
		formData.put("code", code);
		formData.put("redirect_uri", slackRedirectUri);

		SlackOAuthResDto response = slackAuthClient.getAccessToken(formData);

		if (!response.isOk()) {
			log.error("Slack OAuth error: {}", response.getError());
			throw new CustomException(ErrorCode.SLACK_OAUTH_FAILED);
		}
		log.info("Slack OAuth response: {}", response);

		SlackOAuthResDto.IncomingWebhook webhook = response.getIncomingWebhook();
		if (webhook == null) {
			log.error("Incoming webhook information is missing in Slack response.");
			throw new CustomException(ErrorCode.CHANNEL_WEBHOOK_ERROR);
		}

		ProfileNotificationChannelReqDto request = new ProfileNotificationChannelReqDto(
				ChannelType.SLACK,
				webhook.getUrl()
		);

		notificationManagementService.registerChannel(payload, request);
	}

	@Transactional
	public void processDiscordCallback(String code, String state, long userId) {
		OAuthStatePayload payload = oAuthStateService.consumeState(state);

		if (payload.userId() != userId) {
			throw new CustomException(ErrorCode.DISCORD_OAUTH_FAILED);
		}

		Map<String, String> formData = new HashMap<>();
		formData.put("client_id", discordClientId);
		formData.put("client_secret", discordClientSecret);
		formData.put("grant_type", "authorization_code");
		formData.put("code", code);
		formData.put("redirect_uri", discordRedirectUri);

		DiscordOAuthResDto response = discordAuthClient.getAccessToken(formData);
		log.info("Discord OAuth response received.");

		DiscordOAuthResDto.Webhook webhook = response.getWebhook();
		if (webhook == null) {
			log.error("Incoming webhook information is missing in Discord response.");
			throw new CustomException(ErrorCode.CHANNEL_WEBHOOK_ERROR);
		}

		ProfileNotificationChannelReqDto request = new ProfileNotificationChannelReqDto(
				ChannelType.DISCORD,
				webhook.getUrl()
		);

		notificationManagementService.registerChannel(payload, request);
	}

}
