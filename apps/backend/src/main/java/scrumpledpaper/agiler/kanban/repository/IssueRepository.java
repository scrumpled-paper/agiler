package scrumpledpaper.agiler.kanban.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.kanban.entity.Issue;

public interface IssueRepository extends JpaRepository<Issue, Long> {
}
