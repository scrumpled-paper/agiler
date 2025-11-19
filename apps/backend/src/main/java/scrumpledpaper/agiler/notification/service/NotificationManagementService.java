package scrumpledpaper.agiler.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.notification.domain.NotificationSubscription;
import scrumpledpaper.agiler.notification.domain.ProfileNotificationChannel;
import scrumpledpaper.agiler.notification.dto.*;
import scrumpledpaper.agiler.notification.repository.NotificationSubscriptionRepository;
import scrumpledpaper.agiler.notification.repository.ProfileNotificationChannelRepository;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.service.ProjectValidator;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationManagementService {

	private final ProfileNotificationChannelRepository profileNotificationChannelRepository;
	private final NotificationSubscriptionRepository notificationSubscriptionRepository;
	private final ProjectValidator projectValidator;

	@Transactional
	public ProfileNotificationChannel registerChannel(OAuthStatePayload payload, ProfileNotificationChannelRequestDto request) {
		ProfileNotificationChannel channel = profileNotificationChannelRepository
				.findByUserIdAndName(payload.userId(), request.name())
				.map(existing -> {
					existing.updateWebhookUrl(request.webhookUrl());
					return existing;
				})
				.orElseGet(() -> ProfileNotificationChannel.builder()
						.userId(payload.userId())
						.profileId(payload.profileId())
						.channelType(request.channelType())
						.webhookUrl(request.webhookUrl())
						.name(request.name())
						.build()
				);

		return profileNotificationChannelRepository.save(channel);
	}

	@Transactional(readOnly = true)
	public NotificationChannelsResDto getUserChannels(long userId, String projectUrl) {
		ProjectAccessContext accessContext = projectValidator.validateAccess(userId, projectUrl);

		List<ProfileNotificationChannel> channels = profileNotificationChannelRepository.findByProfileId(accessContext.profile().getId());
		return NotificationChannelsResDto.from(channels);
	}

	@Transactional
	public void deleteChannel(long userId, String projectUrl, Long channelId) {
		projectValidator.validateAccess(userId, projectUrl);

		ProfileNotificationChannel channel = profileNotificationChannelRepository.findById(channelId)
				.orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_CHANNEL_NOT_FOUND));
		if (channel.getUserId() != userId) {
			throw new CustomException(ErrorCode.NOTIFICATION_UNAUTHORIZED);
		}
		profileNotificationChannelRepository.delete(channel);
	}

	@Transactional
	public NotificationSubscription subscribe(long userId, String projectUrl, NotificationSubscriptionRequestDto request) {
		ProjectAccessContext accessContext = projectValidator.validateAccess(userId, projectUrl);

		NotificationSubscription subscription = NotificationSubscription.builder()
				.userId(userId)
				.profileId(accessContext.profile().getId())
				.issueId(request.issueId())
				.fromKanbanConfigId(request.fromKanbanConfigId())
				.toKanbanConfigId(request.toKanbanConfigId())
				.build();

		return notificationSubscriptionRepository.save(subscription);
	}

	@Transactional(readOnly = true)
	public SubscriptionsListResDto getUserSubscriptions(long userId, String projectUrl) {
		ProjectAccessContext accessContext = projectValidator.validateAccess(userId, projectUrl);

		return new SubscriptionsListResDto(notificationSubscriptionRepository.findByProfileId(accessContext.profile().getId()));
	}

	@Transactional
	public void unsubscribe(long userId, Long subscriptionId) {
		NotificationSubscription subscription = notificationSubscriptionRepository.findById(subscriptionId)
				.orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_SUBSCRIPTION_NOT_FOUND));
		if (subscription.getUserId() != userId) {
			throw new CustomException(ErrorCode.NOTIFICATION_UNAUTHORIZED);
		}
		notificationSubscriptionRepository.delete(subscription);
	}
}
