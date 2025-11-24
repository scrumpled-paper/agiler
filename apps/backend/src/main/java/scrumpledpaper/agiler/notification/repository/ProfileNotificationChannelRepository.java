package scrumpledpaper.agiler.notification.repository;

import aj.org.objectweb.asm.commons.Remapper;
import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.notification.domain.ChannelType;
import scrumpledpaper.agiler.notification.domain.ProfileNotificationChannel;

import java.util.List;
import java.util.Optional;

public interface ProfileNotificationChannelRepository extends JpaRepository<ProfileNotificationChannel, Long> {
	List<ProfileNotificationChannel> findByUserId(long userId);

	List<ProfileNotificationChannel> findByProfileId(long profileId);

	Optional<ProfileNotificationChannel> findByProfileIdAndChannelType(long profileId, ChannelType channelType);

}
