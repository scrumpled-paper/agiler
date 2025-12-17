package scrumpledpaper.agiler.kanban.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
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
import scrumpledpaper.agiler.kanban.dto.KanbanBoardResDto;
import scrumpledpaper.agiler.kanban.dto.KanbanConfigUpdateReqDto;
import scrumpledpaper.agiler.kanban.dto.SnapshotAvailableResDto;
import scrumpledpaper.agiler.kanban.entity.Issue;
import scrumpledpaper.agiler.kanban.entity.IssueLabel;
import scrumpledpaper.agiler.kanban.entity.IssueProfile;
import scrumpledpaper.agiler.kanban.entity.IssueSnapshotDateMapping;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.kanban.entity.Label;
import scrumpledpaper.agiler.kanban.mapper.IssueMapper;
import scrumpledpaper.agiler.kanban.repository.IssueLabelRepository;
import scrumpledpaper.agiler.kanban.repository.IssueProfileRepository;
import scrumpledpaper.agiler.kanban.repository.IssueRepository;
import scrumpledpaper.agiler.notification.event.IssueStatusChangedEvent;
import scrumpledpaper.agiler.notification.service.NotificationManagementService;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.service.ProfileService;
import scrumpledpaper.agiler.project.service.ProjectValidator;

@Service
@RequiredArgsConstructor
public class IssueService {
	private final ProjectValidator projectValidator;
	private final LabelService labelService;
	private final ProfileService profileService;
	private final SnapshotService snapshotService;
	private final KanbanConfigService kanbanConfigService;
	private final NotificationManagementService notificationManagementService;
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

		snapshotService.countUpOrCreateIssueSnapshotMapping(project, newIssue.getCreatedAt());
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
		ProjectAccessContext accessContext = projectValidator.validateAccess(userId, projectUrl);
		Project project = accessContext.project();

		Issue issue = findIssueById(issueId);

		List<IssueLabel> issueLabels = issueLabelRepository.findAllByIssueId(issueId);
		issueLabelRepository.deleteAll(issueLabels);

		List<IssueProfile> issueProfiles = issueProfileRepository.findAllByIssueId(issueId);
		issueProfileRepository.deleteAll(issueProfiles);

		snapshotService.countDownIssueSnapshotMappingAndDeleteIfZero(project, issue.getCreatedAt());
		issueRepository.delete(issue);
	}

	@Transactional
	public long updateIssueAssignees(long userId, String projectUrl, Long issueId, IssueAssigneesReqDto issueAssigneesReqDto) {
		projectValidator.validateAccess(userId, projectUrl);

		Issue issue = findIssueById(issueId);

		List<IssueProfile> existingIssueProfiles = issueProfileRepository.findAllByIssueId(issueId);
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

		List<IssueLabel> existingIssueLabels = issueLabelRepository.findAllByIssueId(issueId);
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
		return issueRepository.findByProjectIdAndIsDoneFalseAndCreatedAtBetween(projectId, dayStart, dayEnd);
	}

	public void copyToBacklogIssues(Project project, List<Issue> issues) {
		KanbanConfig backlogConfig = kanbanConfigService.getBacklogKanbanConfig(project.getId());

		List<Issue> copyIssues = issues.stream()
			.map(issue -> {
				Issue copyIssue = issueRepository.save(issueMapper.toEntity(project, issue, backlogConfig));
				List<IssueLabel> issueLabels = issueLabelRepository.findAllByIssueId(issue.getId());
				issueLabels.forEach(issueLabel -> {
					IssueLabel copyIssueLabel = issueMapper.toIssueLabel(copyIssue, issueLabel);
					issueLabelRepository.save(copyIssueLabel);
				});

				List<IssueProfile> issueProfiles = issueProfileRepository.findAllByIssueId(issue.getId());
				issueProfiles.forEach(issueProfile -> {
					IssueProfile copyIssueProfile = issueMapper.toIssueProfile(copyIssue, issueProfile);
					issueProfileRepository.save(copyIssueProfile);
				});
				return copyIssue;
			})
			.toList();
	}

	public List<Issue> findIssuesForLatestCreationDay(Long projectId) {
		Optional<Issue> lastCreatedIssue = issueRepository.findFirstByProjectIdAndIsDoneFalse(projectId);
		// 마지막으로 생성된 IsDone되지 않은 이슈가 없는 경우 빈 리스트 반환
		if (lastCreatedIssue.isEmpty()) {
			return List.of();
		}

		LocalDateTime lastCreatedAt = lastCreatedIssue.get().getCreatedAt();
		LocalDate lastCreatedDate = lastCreatedAt.toLocalDate();

		LocalDateTime dayStart = lastCreatedAt.toLocalDate().atStartOfDay();
		LocalDateTime dayEnd = lastCreatedDate.atTime(23, 59, 59);

		return findByProjectIdCreatedAtBetween(projectId, dayStart, dayEnd);
	}

	@Transactional
	public void updateKanbanConfig(long userId, String projectUrl, KanbanConfigUpdateReqDto kanbanConfigUpdateReqDto) {
		ProjectAccessContext projectAccessContext = projectValidator.validateAccess(userId, projectUrl);
		Project project = projectAccessContext.project();

		List<KanbanConfig> existingConfigs = kanbanConfigService.findAllByProject(project);
		int version = existingConfigs.getFirst().getVersion() + 1;
		List<KanbanConfig> updatedConfigs = kanbanConfigService.updateKanbanConfigList(project, version, kanbanConfigUpdateReqDto);

		KanbanConfig backlogConfig = null, defaultConfig = null, doneConfig = null;
		for (KanbanConfig config : updatedConfigs) {
			if (config.isBacklog()) {
				backlogConfig = config;
			} else if (config.isDefaultStatus()) {
				defaultConfig = config;
			} else if (config.getIsDone()) {
				doneConfig = config;
			}
		}

		LocalDateTime startTime = LocalDate.now().atStartOfDay();
		LocalDateTime endTime = startTime.plusDays(1).minusMinutes(1);
		List<Issue> issues = issueRepository.findByProjectIdAndCreatedAtBetween(project.getId(), startTime, endTime);
		for (Issue issue : issues) {
			KanbanConfig newConfig;
			if (issue.getKanbanConfig().isDefaultStatus()) {
				newConfig = defaultConfig;
			} else if (issue.getKanbanConfig().getIsDone()) {
				newConfig = doneConfig;
			} else {
				newConfig = backlogConfig;
			}
			issue.updateKanbanConfig(newConfig);
		}
		issueRepository.saveAll(issues);
		kanbanConfigService.deleteAllKanbanConfigs(existingConfigs);
	}

	@Transactional
	public long issueSnapshotAndResetForToday(long userId, String projectUrl) {
		ProjectAccessContext projectAccessContext = projectValidator.validateAccess(userId, projectUrl);
		Project project = projectAccessContext.project();

		LocalDateTime now = LocalDateTime.now();
		LocalDate today = now.toLocalDate();
		long timeUntilTarget = Duration.between(now, today.atTime(23, 59, 59)).toMillis();

		Optional<IssueSnapshotDateMapping> alreadySnapshot = snapshotService.findByProjectIdAndSnapshotDate(project.getId(), today);
		if (alreadySnapshot.isPresent()) {
			return timeUntilTarget;
		}

		List<Issue> issues = findIssuesForLatestCreationDay(project.getId());
		if (issues.isEmpty()) {
			return timeUntilTarget;
		}

		copyToBacklogIssues(project, issues);

		int count = issues.size();
		snapshotService.saveSnapshotMapping(project, today, count);

		return timeUntilTarget;
	}

	@Transactional(readOnly = true)
	public SnapshotAvailableResDto getAvailableSnapshotDates(long userId, String projectUrl, LocalDate date) {
		ProjectAccessContext projectAccessContext = projectValidator.validateAccess(userId, projectUrl);
		Project project = projectAccessContext.project();

		LocalDate startDate = date.withDayOfMonth(1);
		LocalDate endDate = YearMonth.from(date).atEndOfMonth();

		List<LocalDate> availableDates = snapshotService.issueSnapshotDateMappingsByProjectIdAndBetween(
			project.getId(),
			startDate,
			endDate
		);
		return new SnapshotAvailableResDto(availableDates);
	}

	@Transactional(readOnly = true)
	public KanbanBoardResDto getKanbanBoard(long userId, String projectUrl, LocalDate date) {
		ProjectAccessContext projectAccessContext = projectValidator.validateAccess(userId, projectUrl);
		Project project = projectAccessContext.project();

		List<Issue> issues = issueRepository.findAllByProjectIdWithRelations(project.getId());
		List<KanbanBoardResDto.LabelDto> labelDtos = labelService.getProjectLabelsAsKanbanDto(project);
		List<KanbanBoardResDto.ProfileDto> profileDtos = profileService.getProjectProfilesAsKanbanDto(project);
		List<KanbanBoardResDto.KanbanConfigDto> kanbanConfigDtos = kanbanConfigService.getKanbanConfigsAsKanbanDto(project);

		Map<Long, List<KanbanBoardResDto.IssueNoti>> issueNotisMap =
			notificationManagementService.getIssueNotisMapAsKanbanDto(issues);

		List<KanbanBoardResDto.IssueDto> issueDtos = issues.stream()
			.map(issue -> issueMapper.toKanbanBoardIssueDto(issue, issueNotisMap.getOrDefault(issue.getId(), List.of())))
			.toList();

		return new KanbanBoardResDto(kanbanConfigDtos, profileDtos, labelDtos, issueDtos);
	}
}
