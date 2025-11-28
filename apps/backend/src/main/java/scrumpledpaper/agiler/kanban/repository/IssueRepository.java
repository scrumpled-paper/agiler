package scrumpledpaper.agiler.kanban.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.kanban.entity.Issue;

public interface IssueRepository extends JpaRepository<Issue, Long> {
	Optional<Issue> findByProjectId(Long projectId);
}
