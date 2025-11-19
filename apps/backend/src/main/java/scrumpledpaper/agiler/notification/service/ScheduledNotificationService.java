package scrumpledpaper.agiler.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.kanban.entity.Issue;
import scrumpledpaper.agiler.kanban.repository.IssueRepository;
import scrumpledpaper.agiler.notification.domain.ProfileNotificationChannel;
import scrumpledpaper.agiler.notification.domain.ScheduledNotification;
import scrumpledpaper.agiler.notification.dto.ScheduleNotificationReqDto;
import scrumpledpaper.agiler.notification.repository.ProfileNotificationChannelRepository;
import scrumpledpaper.agiler.notification.repository.ScheduledNotificationRepository;
import scrumpledpaper.agiler.notification.sender.NotificationSender;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.service.ProjectValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledNotificationService {

	private final ProjectValidator projectValidator;
    private final ScheduledNotificationRepository scheduledNotificationRepository;
    private final ProfileNotificationChannelRepository profileNotificationChannelRepository;
    private final IssueRepository issueRepository;
    private final Map<String, NotificationSender> notificationSenders;

    @Scheduled(fixedDelay = 60000) // 1분마다 실행
    @Transactional
    public void processScheduledNotifications() {
        log.info("Processing scheduled notifications at {}", LocalDateTime.now());
        List<ScheduledNotification> notificationsToProcess = scheduledNotificationRepository
                .findAllByNotificationTimeBeforeAndIsSentIsFalse(LocalDateTime.now());

        for (ScheduledNotification notification : notificationsToProcess) {
            try {
                long userId = notification.getUserId();
                List<ProfileNotificationChannel> channels = profileNotificationChannelRepository.findByUserId(userId);

                if (channels.isEmpty()) {
                    log.warn("User {} has no notification channels configured for scheduled notification {}.", userId, notification.getId());
                    notification.markAsSent(); // 채널이 없으면 발송 처리하고 다음으로 넘어감
                    continue;
                }

                for (ProfileNotificationChannel channel : channels) {
                    NotificationSender sender = notificationSenders.get(channel.getChannelType().name());
                    if (sender != null) {
                        sender.send(channel.getWebhookUrl(), notification.getMessage());
                        log.info("Scheduled notification {} sent to user {} via {} channel.", notification.getId(), userId, channel.getChannelType());
                    } else {
                        log.warn("No sender found for channel type: {} for scheduled notification {}.", channel.getChannelType(), notification.getId());
                    }
                }
                notification.markAsSent(); // 발송 완료 처리
            } catch (Exception e) {
                log.error("Error processing scheduled notification {}: {}", notification.getId(), e.getMessage(), e);
                // TODO: 재시도 로직 또는 실패 처리 (예: 실패 횟수 증가, 특정 횟수 이상 실패 시 비활성화)
            }
        }
        scheduledNotificationRepository.saveAll(notificationsToProcess);
    }

    @Transactional
    public ScheduledNotification scheduleNotification(Long userId, String projectUrl,  ScheduleNotificationReqDto request) {
		ProjectAccessContext accessContext = projectValidator.validateAccess(userId, projectUrl);

		LocalDateTime notificationTime = LocalDateTime.now().plusMinutes(request.delayInMinutes());

        Issue issue = issueRepository.findById(request.issueId())
                .orElseThrow(() -> new CustomException(ErrorCode.ISSUE_NOT_FOUND));

        ScheduledNotification scheduledNotification = ScheduledNotification.builder()
                .userId(userId)
                .issueId(issue.getId())
				.profileId(accessContext.profile().getId())
                .notificationTime(notificationTime)
                .message(request.message())
                .build();

        return scheduledNotificationRepository.save(scheduledNotification);
    }

}
