package scrumpledpaper.agiler.kanban.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import scrumpledpaper.agiler.kanban.entity.KanbanConfig;

public interface KanbanConfigRepository extends JpaRepository<KanbanConfig, Long> {
	Optional<KanbanConfig> findByProjectIdAndDefaultStatusTrue(Long projectId);

	List<KanbanConfig> findAllByProjectId(Long id);

	List<KanbanConfig> findByProjectIdOrderByPriorityAsc(Long id);

	Optional<KanbanConfig> findByProjectIdAndBacklogTrue(Long projectId);
}
