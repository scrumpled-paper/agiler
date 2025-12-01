package scrumpledpaper.agiler.kanban.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.kanban.dto.IssueAssigneesReqDto;
import scrumpledpaper.agiler.kanban.dto.IssueCreateReqDto;
import scrumpledpaper.agiler.kanban.dto.IssueKanbanConfigUpdateReqDto;
import scrumpledpaper.agiler.kanban.dto.IssueLabelsReqDto;
import scrumpledpaper.agiler.kanban.dto.IssueUpdateReqDto;
import scrumpledpaper.agiler.kanban.entity.Issue;
import scrumpledpaper.agiler.kanban.entity.IssueLabel;
import scrumpledpaper.agiler.kanban.entity.IssueProfile;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.kanban.entity.Label;
import scrumpledpaper.agiler.kanban.mapper.IssueMapper;
import scrumpledpaper.agiler.kanban.repository.IssueLabelRepository;
import scrumpledpaper.agiler.kanban.repository.IssueProfileRepository;
import scrumpledpaper.agiler.kanban.repository.IssueRepository;
import scrumpledpaper.agiler.notification.event.IssueStatusChangedEvent;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.service.ProjectValidator;

@Service
@RequiredArgsConstructor
public class IssueService {
	private final ProjectValidator projectValidator;
	private final LabelService labelService;
	private final KanbanConfigService kanbanConfigService;
	private final IssueRepository issueRepository;
	private final IssueLabelRepository issueLabelRepository;
	private final IssueProfileRepository issueProfileRepository;
	private final IssueMapper issueMapper;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public long createIssue(long userId, String projectUrl, IssueCreateReqDto issueCreateReqDto) {
		ProjectAccessContext projectAccessContext = projectValidator.validateAccess(userId, projectUrl);
		Project project = projectAccessContext.project();

		KanbanConfig defaultKanbanConfig = kanbanConfigService.getDefaultStatusKanbanConfig(project.getId());
		Issue newIssue = issueRepository.save(issueMapper.toEntity(project, defaultKanbanConfig, issueCreateReqDto));

		List<Label> labels = labelService.getLabelsByIds(issueCreateReqDto.labels());
		List<IssueLabel> issueLabels = issueMapper.toIssueLabel(newIssue, labels);
		issueLabelRepository.saveAll(issueLabels);

		List<Profile> assignees = projectValidator.projectMembersByIds(project, issueCreateReqDto.assignees());
		List<IssueProfile> issueProfiles = issueMapper.toIssueProfile(newIssue, assignees);
		issueProfileRepository.saveAll(issueProfiles);

		return newIssue.getId();
	}

	@Transactional
	public long updateIssue(long userId, String projectUrl, IssueUpdateReqDto issueUpdateReqDto) {
		projectValidator.validateAccess(userId, projectUrl);

		Issue issue = findIssueById(issueUpdateReqDto.issueId());

		issue.update(
			issueUpdateReqDto.title(),
			issueUpdateReqDto.contents()
		);
		return issue.getId();
	}

	public Issue findIssueById(Long issueId) {
		return issueRepository.findById(issueId)
			.orElseThrow(() -> new CustomException(ErrorCode.ISSUE_NOT_FOUND));
	}

	@Transactional
	public void deleteIssue(long userId, String projectUrl, Long issueId) {
		projectValidator.validateAccess(userId, projectUrl);

		List<IssueLabel> issueLabels = issueLabelRepository.findByIssueId(issueId);
		issueLabelRepository.deleteAll(issueLabels);

		List<IssueProfile> issueProfiles = issueProfileRepository.findByIssueId(issueId);
		issueProfileRepository.deleteAll(issueProfiles);

		Issue issue = findIssueById(issueId);
		issueRepository.delete(issue);
	}

	@Transactional
	public long updateIssueAssignees(long userId, String projectUrl, Long issueId, IssueAssigneesReqDto issueAssigneesReqDto) {
		projectValidator.validateAccess(userId, projectUrl);

		Issue issue = findIssueById(issueId);

		List<IssueProfile> existingIssueProfiles = issueProfileRepository.findByIssueId(issueId);
		issueProfileRepository.deleteAll(existingIssueProfiles);

		List<Profile> assignees = projectValidator.projectMembersByIds(issue.getProject(), issueAssigneesReqDto.assignees());
		List<IssueProfile> newIssueProfiles = issueMapper.toIssueProfile(issue, assignees);
		issueProfileRepository.saveAll(newIssueProfiles);

		return issue.getId();
	}

	@Transactional
	public long updateIssueLabels(long userId, String projectUrl, Long issueId, IssueLabelsReqDto issueLabelsReqDto) {
		projectValidator.validateAccess(userId, projectUrl);

		Issue issue = findIssueById(issueId);

		List<IssueLabel> existingIssueLabels = issueLabelRepository.findByIssueId(issueId);
		issueLabelRepository.deleteAll(existingIssueLabels);

		List<Label> labels = labelService.getLabelsByIds(issueLabelsReqDto.labels());
		List<IssueLabel> newIssueLabels = issueMapper.toIssueLabel(issue, labels);
		issueLabelRepository.saveAll(newIssueLabels);

		return issue.getId();
	}

	@Transactional
	public long updateIssueKanbanConfig(long userId, String projectUrl, Long issueId, IssueKanbanConfigUpdateReqDto issueKanbanConfigUpdateReqDto) {
		ProjectAccessContext accessContext = projectValidator.validateAccess(userId, projectUrl);

		Issue issue = findIssueById(issueId);
		KanbanConfig fromKanbanConfig = issue.getKanbanConfig();
		KanbanConfig toKanbanConfig = kanbanConfigService.getKanbanConfigById(issueKanbanConfigUpdateReqDto.kanbanConfigId());
		issue.updateKanbanConfig(toKanbanConfig);

		eventPublisher.publishEvent(new IssueStatusChangedEvent(
			this,
			issueId,
			fromKanbanConfig.getId(),
			toKanbanConfig.getId(),
			userId,
			accessContext.project().getId()
		));

		return issue.getId();
	}

	public List<Issue> findByProjectIdCreatedAtBetween(Long projectId, LocalDateTime dayStart, LocalDateTime dayEnd) {
		return issueRepository.findByProjectIdAndCreatedAtBetween(projectId, dayStart, dayEnd);
	}

	public void copyToBacklogIssuesAndDeleteOriginalIssues(Long projectId, List<Issue> issues) {
		KanbanConfig backlogConfig = kanbanConfigService.getBacklogKanbanConfig(projectId);

		List<Issue> newBacklogIssues = issues.stream()
			.filter(issue -> Boolean.FALSE.equals(issue.getIsDone()))
			.map(issue -> issueMapper.toEntity(issue, backlogConfig))
			.toList();
		issueRepository.saveAll(newBacklogIssues);
		issueRepository.deleteAll(issues);
	}

	public List<Issue> findIssuesForLatestCreationDay(Long projectId, int issueSnapshotStartHour) {
		Optional<Issue> lastCreatedIssue = issueRepository.findFirstByProjectIdOrderByCreatedAtDesc(projectId);
		if (lastCreatedIssue.isEmpty()) {
			return List.of();
		}

		LocalDateTime lastCreatedAt = lastCreatedIssue.get().getCreatedAt();
		LocalDate lastCreatedDate = lastCreatedAt.toLocalDate();

		if (lastCreatedAt.getHour() < issueSnapshotStartHour) {
			lastCreatedDate = lastCreatedDate.minusDays(1);
		}

		LocalDateTime dayStart = lastCreatedDate.atTime(issueSnapshotStartHour, 0);
		LocalDateTime dayEnd = lastCreatedDate.plusDays(1)
			.atTime(issueSnapshotStartHour, 0)
			.minusNanos(1);

		return findByProjectIdCreatedAtBetween(projectId, dayStart, dayEnd);
	}
}
