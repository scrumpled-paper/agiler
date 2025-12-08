package scrumpledpaper.agiler.kanban.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import scrumpledpaper.agiler.kanban.entity.IssueSnapshotDateMapping;
import scrumpledpaper.agiler.project.entity.Project;

public interface IssueSnapshotDateMappingRepository extends JpaRepository<IssueSnapshotDateMapping, Long> {
	Optional<IssueSnapshotDateMapping> findByProjectIdAndSnapshotDate(Long id, LocalDate snapshotDate);

	List<IssueSnapshotDateMapping> findByProjectIdAndSnapshotDateBetweenOrderBySnapshotDateDesc(
		Long projectId,
		LocalDate startDate,
		LocalDate endDate
	);
}
