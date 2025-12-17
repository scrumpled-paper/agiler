package scrumpledpaper.agiler.kanban.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.kanban.entity.Issue;

public interface IssueRepository extends JpaRepository<Issue, Long> {
	Optional<Issue> findByProjectId(Long projectId);

	List<Issue> findByProjectIdAndIsDoneFalseAndCreatedAtBetween(Long projectId, LocalDateTime dayStart, LocalDateTime dayEnd);

	Optional<Issue> findFirstByProjectIdAndIsDoneFalse(Long projectId);

	List<Issue> findAllByProjectId(Long id);

	List<Issue> findByProjectIdAndCreatedAtBetween(
		Long projectId,
		LocalDateTime startTime,
		LocalDateTime endTime
	);
}
