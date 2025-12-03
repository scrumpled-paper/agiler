package scrumpledpaper.agiler.kanban.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.kanban.entity.Issue;

public interface IssueRepository extends JpaRepository<Issue, Long> {
	Optional<Issue> findByProjectId(Long projectId);

	List<Issue> findByProjectIdAndCreatedAtBetween(Long projectId, LocalDateTime dayStart, LocalDateTime dayEnd);

	Optional<Issue> findFirstByProjectIdOrderByCreatedAtDesc(Long projectId);

	List<Issue> findAllByProjectId(Long id);
}
