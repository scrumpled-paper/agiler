package scrumpledpaper.agiler.template.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.template.entity.MeetingTemplate;

public interface MeetingTemplateRepository extends JpaRepository<MeetingTemplate, Long> {
	List<MeetingTemplate> findByProjectId(Long projectId);
}
