package scrumpledpaper.agiler.kanban.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import scrumpledpaper.agiler.kanban.entity.IssueSnapshot;

public interface IssueSnapshotRepository extends JpaRepository<IssueSnapshot, Long> {
}
