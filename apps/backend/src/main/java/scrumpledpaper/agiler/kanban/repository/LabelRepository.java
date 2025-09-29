package scrumpledpaper.agiler.kanban.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.kanban.entity.Label;

public interface LabelRepository extends JpaRepository<Label, Long> {
}
