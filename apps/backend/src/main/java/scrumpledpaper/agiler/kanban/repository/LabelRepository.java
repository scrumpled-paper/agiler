package scrumpledpaper.agiler.kanban.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import scrumpledpaper.agiler.kanban.entity.Label;

public interface LabelRepository extends JpaRepository<Label, Long> {
	List<Label> findByProjectId(Long projectId);
}
