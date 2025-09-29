package scrumpledpaper.agiler.template.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.template.entity.IssueTemplate;

public interface IssueTemplateRepository extends JpaRepository<IssueTemplate, Long> {
}
