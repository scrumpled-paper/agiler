package scrumpledpaper.agiler.kanban.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import scrumpledpaper.agiler.kanban.entity.IssueLabel;

public interface IssueLabelRepository extends JpaRepository<IssueLabel, Long> {
	List<IssueLabel> findAllByIssueId(Long issueId);

	@Query("""
		SELECT il
		FROM IssueLabel il
		LEFT JOIN FETCH il.label
		WHERE il.issue.id = :issueId
	""")
	List<IssueLabel> findAllByIssueIdWithRelationLabel(Long issueId);
}
