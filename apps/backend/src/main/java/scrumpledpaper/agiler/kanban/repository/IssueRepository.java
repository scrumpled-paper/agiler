package scrumpledpaper.agiler.kanban.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import scrumpledpaper.agiler.kanban.entity.Issue;

public interface IssueRepository extends JpaRepository<Issue, Long> {
	Optional<Issue> findByProjectId(Long projectId);

	List<Issue> findByProjectIdAndIsDoneFalseAndCreatedAtBetween(Long projectId, LocalDateTime dayStart, LocalDateTime dayEnd);

	Optional<Issue> findFirstByProjectIdAndIsDoneFalse(Long projectId);

	List<Issue> findAllByProjectId(Long id);

	@Query("""
    SELECT DISTINCT i FROM Issue i
        LEFT JOIN FETCH i.assignees ia
        LEFT JOIN FETCH ia.profile
        LEFT JOIN FETCH i.kanbanConfig
    WHERE i.project.id = :projectId
        AND i.createdAt >= :startDateTime
        AND i.createdAt < :endDateTime
    """)
	List<Issue> findAllByProjectIdAndCreatedAtBetweenWithRelations(
		@Param("projectId") Long projectId,
		@Param("startDateTime") LocalDateTime startDateTime,
		@Param("endDateTime") LocalDateTime endDateTime
	);

	List<Issue> findByProjectIdAndCreatedAtBetween(
		Long projectId,
		LocalDateTime startTime,
		LocalDateTime endTime
	);
}

