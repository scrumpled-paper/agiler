package scrumpledpaper.agiler.kanban.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import scrumpledpaper.agiler.kanban.entity.IssueLabel;

public interface IssueLabelRepository extends JpaRepository<IssueLabel, Long> {
	List<IssueLabel> findAllByIssueId(Long issueId);
}
