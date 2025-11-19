package scrumpledpaper.agiler.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.notification.domain.ChannelType;
import scrumpledpaper.agiler.notification.domain.NotificationSubscription;
import scrumpledpaper.agiler.notification.domain.ProfileNotificationChannel;
import scrumpledpaper.agiler.notification.dto.OAuthStatePayload;
import scrumpledpaper.agiler.notification.event.IssueStatusChangedEvent;
import scrumpledpaper.agiler.notification.repository.NotificationSubscriptionRepository;
import scrumpledpaper.agiler.notification.repository.ProfileNotificationChannelRepository;
import scrumpledpaper.agiler.notification.sender.NotificationSender;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.repository.ProfileRepository;
import scrumpledpaper.agiler.project.service.ProjectValidator;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.repository.UserRepository;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final OAuthStateService oAuthStateService;
	private final ProjectValidator projectValidator;
	private final NotificationSubscriptionRepository subscriptionRepository;
	private final ProfileNotificationChannelRepository channelRepository;
	private final ProfileRepository profileRepository;
	private final Map<String, NotificationSender> notificationSenders;

	@Value("${spring.security.oauth2.client.registration.slack.client-id}")
	private String slackClientId;

	@Value("${spring.security.oauth2.client.registration.slack.scope}")
	private String slackScope;

	@Value("${spring.security.oauth2.client.registration.slack.redirect-uri}")
	private String slackRedirectUri;

	@Value("${spring.security.oauth2.client.provider.slack.authorization-uri}")
	private String slackAuthorizationUri;

	@Value("${spring.security.oauth2.client.registration.discord.client-id}")
	private String discordClientId;

	@Value("${spring.security.oauth2.client.registration.discord.scope}")
	private String discordScope;

	@Value("${spring.security.oauth2.client.registration.discord.redirect-uri}")
	private String discordRedirectUri;

	@Value("${spring.security.oauth2.client.provider.discord.authorization-uri}")
	private String discordAuthorizationUri;

	public String buildCallbackUri(long userId, String projectUrl, ChannelType type) {
		ProjectAccessContext accessContext = projectValidator.validateAccess(userId, projectUrl);
		String state = oAuthStateService.createState(userId, accessContext.profile().getId());

		if (type == ChannelType.SLACK) {
			return UriComponentsBuilder.fromUriString(slackAuthorizationUri)
					.queryParam("client_id", slackClientId)
					.queryParam("scope", slackScope)
					.queryParam("redirect_uri", slackRedirectUri)
					.queryParam("state", state)
					.toUriString();
		}
		if (type == ChannelType.DISCORD) {
			return UriComponentsBuilder.fromUriString(discordAuthorizationUri)
					.queryParam("client_id", discordClientId)
					.queryParam("scope", discordScope)
					.queryParam("redirect_uri", discordRedirectUri)
					.queryParam("state", state)
					.toUriString();
		}

		OAuthStatePayload payload = oAuthStateService.consumeState(state);
		log.error("Unsupported channel type for OAuth: {}, payload: {} ", type, payload);
		throw new CustomException(ErrorCode.NOTIFICATION_CHANNEL_NOT_FOUND);
	}

	@Async
	@EventListener
	@Transactional(readOnly = true)
	public void handleIssueStatusChangedEvent(IssueStatusChangedEvent event) {
		log.info("Handling IssueStatusChangedEvent for issueId: {}", event.getIssueId());

		// 1. 이벤트 조건과 일치하는 모든 구독 조회
		List<NotificationSubscription> subscriptions = subscriptionRepository
				.findSubscriptionsForIssueStatusChange(
						event.getProjectId(),
						event.getIssueId(),
						event.getFromKanbanConfigId(),
						event.getToKanbanConfigId()
				);

		if (subscriptions.isEmpty()) {
			log.info("No matching subscriptions found for event: {}", event);
			return;
		}

		String updaterName = profileRepository.findById(event.getProfileId())
				.map(Profile::getNickname)
				.orElse("Unknown User");

		// 2. 각 구독자에게 알림 발송
		for (NotificationSubscription subscription : subscriptions) {
			long subscriberId = subscription.getUserId();
			List<ProfileNotificationChannel> channels = channelRepository.findByUserId(subscriberId);

			if (channels.isEmpty()) {
				log.warn("User {} has no notification channels configured.", subscriberId);
				continue;
			}

			String message = String.format(
					"[Project #%d] Issue #%d status changed from '%s' to '%s' by %s.",
					event.getProjectId(),
					event.getIssueId(),
					event.getFromKanbanConfigId(),
					event.getToKanbanConfigId(),
					updaterName
			);

			for (ProfileNotificationChannel channel : channels) {
				NotificationSender sender = notificationSenders.get(channel.getChannelType().name());
				if (sender != null) {
					sender.send(channel.getWebhookUrl(), message);
					log.info("Notification sent to user {} via {} channel for subscription {}.", subscriberId, channel.getChannelType(), subscription.getId());
				} else {
					log.warn("No sender found for channel type: {}", channel.getChannelType());
				}
			}
		}
	}

}
