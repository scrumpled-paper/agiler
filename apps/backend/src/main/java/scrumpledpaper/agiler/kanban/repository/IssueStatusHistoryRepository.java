package scrumpledpaper.agiler.kanban.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.kanban.entity.IssueStatusHistory;

public interface IssueStatusHistoryRepository extends JpaRepository<IssueStatusHistory, Long> {
}
