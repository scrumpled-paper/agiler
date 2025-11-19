package scrumpledpaper.agiler.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.notification.domain.ProfileNotificationChannel;

import java.util.List;
import java.util.Optional;

public interface ProfileNotificationChannelRepository extends JpaRepository<ProfileNotificationChannel, Long> {
	List<ProfileNotificationChannel> findByUserId(long userId);
    Optional<ProfileNotificationChannel> findByUserIdAndName(long userId, String name);

	List<ProfileNotificationChannel> findByProfileId(long profileId);
}
