package scrumpledpaper.agiler.kanban.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import scrumpledpaper.agiler.kanban.entity.KanbanConfigSnapshot;

public interface KanbanConfigSnapshotRepository extends JpaRepository<KanbanConfigSnapshot, Long> {
	List<KanbanConfigSnapshot> findByProjectId(Long projectId);
}
