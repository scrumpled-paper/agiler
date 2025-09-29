package scrumpledpaper.agiler.template.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.template.entity.MeetingTemplate;

public interface MeetingTemplateRepository extends JpaRepository<MeetingTemplate, Long> {
}
