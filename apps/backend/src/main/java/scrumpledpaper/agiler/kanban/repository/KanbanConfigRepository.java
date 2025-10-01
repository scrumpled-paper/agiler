package scrumpledpaper.agiler.kanban.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;

public interface KanbanConfigRepository extends JpaRepository<KanbanConfig, Long> {
}
