package scrumpledpaper.agiler.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import scrumpledpaper.agiler.notification.domain.NotificationSubscription;

public interface NotificationSubscriptionRepository extends JpaRepository<NotificationSubscription, Long> {

	@Query("SELECT ns FROM NotificationSubscription ns WHERE " +
			"ns.profileId = :profileId AND " +
			"(ns.issueId = :issueId) AND " +
			"(ns.fromKanbanConfigId = :fromKanbanConfigId) AND " +
			"(ns.toKanbanConfigId = :toKanbanConfigId)")
	List<NotificationSubscription> findSubscriptionsForIssueStatusChange(
			@Param("profileId") Long profileId,
			@Param("issueId") Long issueId,
			@Param("fromKanbanConfigId") Long fromKanbanConfigId,
			@Param("toKanbanConfigId") Long toKanbanConfigId
	);

	List<NotificationSubscription> findByProfileId(long profileId);

	List<NotificationSubscription> findIssueNotisByIssueIdIn(List<Long> issueIds);
}
