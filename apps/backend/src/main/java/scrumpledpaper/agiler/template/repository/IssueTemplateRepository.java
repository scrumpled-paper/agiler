package scrumpledpaper.agiler.template.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.template.entity.IssueTemplate;

public interface IssueTemplateRepository extends JpaRepository<IssueTemplate, Long> {
	List<IssueTemplate> findByProjectId(Long projectId);

	List<IssueTemplate> findAllByProjectId(Long id);
}
