package scrumpledpaper.agiler.template.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.template.entity.RetroTemplate;

public interface RetroTemplateRepository extends JpaRepository<RetroTemplate, Long> {
	List<RetroTemplate> findByProjectId(Long projectId);
}
