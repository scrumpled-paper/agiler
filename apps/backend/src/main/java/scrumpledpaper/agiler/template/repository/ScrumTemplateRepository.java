package scrumpledpaper.agiler.template.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.template.entity.ScrumTemplate;

public interface ScrumTemplateRepository extends JpaRepository<ScrumTemplate, Long> {
}
