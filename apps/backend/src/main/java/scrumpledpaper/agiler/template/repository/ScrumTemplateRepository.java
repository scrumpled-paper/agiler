package scrumpledpaper.agiler.template.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.template.entity.ScrumTemplate;

public interface ScrumTemplateRepository extends JpaRepository<ScrumTemplate, Long> {
	List<ScrumTemplate> findByProjectId(Long projectId);
}
