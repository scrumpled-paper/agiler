package scrumpledpaper.agiler.kanban.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import scrumpledpaper.agiler.kanban.entity.IssueProfile;

public interface IssueProfileRepository extends JpaRepository<IssueProfile, Long> {
	List<IssueProfile> findByIssueId(Long id);
}
