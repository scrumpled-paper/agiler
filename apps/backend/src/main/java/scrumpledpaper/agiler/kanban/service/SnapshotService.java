package scrumpledpaper.agiler.kanban.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.kanban.entity.Issue;
import scrumpledpaper.agiler.kanban.entity.IssueSnapshotDateMapping;
import scrumpledpaper.agiler.kanban.mapper.IssueMapper;
import scrumpledpaper.agiler.kanban.repository.IssueSnapshotDateMappingRepository;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.service.ProjectValidator;

@Service
@RequiredArgsConstructor
public class SnapshotService {
	private final IssueService issueService;
	private final IssueSnapshotDateMappingRepository issueSnapshotDateMappingRepository;
	private final IssueMapper issueMapper;
	private final ProjectValidator projectValidator;

	private static final int ISSUE_SNAPSHOT_START_HOUR = 6;

	@Transactional
	public long issueSnapshotAndResetForToday(long userId, String projectUrl) {
		ProjectAccessContext projectAccessContext = projectValidator.validateAccess(userId, projectUrl);
		Project project = projectAccessContext.project();

		LocalDate today = LocalDate.now();
		LocalDateTime targetTime = today
			.plusDays(1)
			.atTime(ISSUE_SNAPSHOT_START_HOUR, 0)
			.minusSeconds(1);
		long timeUntilTarget = Duration.between(LocalDateTime.now(), targetTime).toMillis();

		Optional<IssueSnapshotDateMapping> alreadySnapshot = issueSnapshotDateMappingRepository
			.findByProjectIdAndSnapshotDate(project.getId(), today);
		if (alreadySnapshot.isPresent()) {
			return timeUntilTarget;
		}

		List<Issue> issues = issueService.findIssuesForLatestCreationDay(project.getId(), ISSUE_SNAPSHOT_START_HOUR);
		if (issues.isEmpty()) {
			return timeUntilTarget;
		}

		issueService.copyToBacklogIssues(project, issues);

		int count = issues.size();
		issueSnapshotDateMappingRepository.save(issueMapper.toIssueSnapshotDateMapping(project, today, count));

		return timeUntilTarget;
	}


	public void kanbanConfigSnapshot(List<KanbanConfig> kanbanConfigs) {
		List<KanbanConfigSnapshot> snapshots = kanbanConfigs.stream()
			.map(kanbanConfigMapper::toKanbanConfigSnapshot)
			.toList();

		kanbanConfigSnapshotRepository.saveAll(snapshots);
	}
}
