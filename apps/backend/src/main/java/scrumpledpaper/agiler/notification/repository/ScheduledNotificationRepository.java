package scrumpledpaper.agiler.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.notification.domain.ScheduledNotification;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledNotificationRepository extends JpaRepository<ScheduledNotification, Long> {
    // 발송 시간이 되었지만 아직 발송되지 않은 모든 예약 알림 조회
    List<ScheduledNotification> findAllByNotificationTimeBeforeAndIsSentIsFalse(LocalDateTime now);

	List<ScheduledNotification> findByProfileId(long profileId);
}
