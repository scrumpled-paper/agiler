package scrumpledpaper.agiler.kanban.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.kanban.entity.IssueSnapshotDateMapping;
import scrumpledpaper.agiler.kanban.mapper.IssueMapper;
import scrumpledpaper.agiler.kanban.repository.IssueSnapshotDateMappingRepository;
import scrumpledpaper.agiler.project.entity.Project;

@Service
@RequiredArgsConstructor
public class SnapshotService {
	private final IssueSnapshotDateMappingRepository issueSnapshotDateMappingRepository;
	private final IssueMapper issueMapper;

	public void saveSnapshotMapping(Project project, LocalDate snapshotDate, int count) {
		issueSnapshotDateMappingRepository.save(issueMapper.toIssueSnapshotDateMapping(project, snapshotDate, count));
	}

	public Optional<IssueSnapshotDateMapping> findByProjectIdAndSnapshotDate(Long projectId, LocalDate snapshotDate) {
		return issueSnapshotDateMappingRepository.findByProjectIdAndSnapshotDate(projectId, snapshotDate);
	}

	public void countDownIssueSnapshotMappingAndDeleteIfZero(Project project, LocalDateTime issueCreatedAt) {
		LocalDate issueSnapshotDate = issueCreatedAt.toLocalDate();

		Optional<IssueSnapshotDateMapping> mapping = issueSnapshotDateMappingRepository
			.findByProjectIdAndSnapshotDate(project.getId(), issueSnapshotDate);
		if (mapping.isEmpty()) {
			return;
		}

		mapping.get().decrementIssueSnapshotMappingCount();

		if (mapping.get().getIssueCount() <= 0) {
			issueSnapshotDateMappingRepository.delete(mapping.get());
		}
	}

	public List<LocalDate> issueSnapshotDateMappingsByProjectIdAndBetween(Long projectId, LocalDate startDate, LocalDate endDate) {
		return issueSnapshotDateMappingRepository
			.findByProjectIdAndSnapshotDateBetweenOrderBySnapshotDateDesc(projectId, startDate, endDate)
			.stream()
			.map(IssueSnapshotDateMapping::getSnapshotDate)
			.toList();
	}

	public void countUpIssueSnapshotMapping(Project project, LocalDateTime createdAt) {
		LocalDate issueSnapshotDate = createdAt.toLocalDate();

		Optional<IssueSnapshotDateMapping> mapping = issueSnapshotDateMappingRepository
			.findByProjectIdAndSnapshotDate(project.getId(), issueSnapshotDate);
		if (mapping.isEmpty()) {
			issueSnapshotDateMappingRepository.save(issueMapper.toIssueSnapshotDateMapping(project, issueSnapshotDate, 1));
			return;
		}

		mapping.get().incrementIssueSnapshotMappingCount();
	}
}
