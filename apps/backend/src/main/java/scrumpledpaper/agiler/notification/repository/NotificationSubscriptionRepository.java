package scrumpledpaper.agiler.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import scrumpledpaper.agiler.notification.domain.NotificationSubscription;

import java.util.List;

public interface NotificationSubscriptionRepository extends JpaRepository<NotificationSubscription, Long> {

	@Query("SELECT ns FROM NotificationSubscription ns WHERE " +
			"ns.profileId = :profileId AND " +
			"(ns.issueId IS NULL OR ns.issueId = :issueId) AND " +
			"(ns.fromStatus IS NULL OR ns.fromStatus = :fromStatus) AND " +
			"(ns.toStatus IS NULL OR ns.toStatus = :toStatus)")
	List<NotificationSubscription> findSubscriptionsForIssueStatusChange(
			@Param("profileId") Long profileId,
			@Param("issueId") Long issueId,
			@Param("fromStatus") String fromStatus,
			@Param("toStatus") String toStatus
	);

	List<NotificationSubscription> findByUserId(long userId);

	List<NotificationSubscription> findByProfileId(long profileId);
}
