package scrumpledpaper.agiler.kanban.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.kanban.dto.IssueAssigneesReqDto;
import scrumpledpaper.agiler.kanban.dto.IssueCreateReqDto;
import scrumpledpaper.agiler.kanban.dto.IssueKanbanConfigReqDto;
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

	private Issue findIssueById(Long issueId) {
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
	public void updateIssueAssignees(long userId, String projectUrl, Long issueId, IssueAssigneesReqDto issueAssigneesReqDto) {
		projectValidator.validateAccess(userId, projectUrl);

		Issue issue = findIssueById(issueId);

		List<IssueProfile> existingIssueProfiles = issueProfileRepository.findByIssueId(issueId);
		issueProfileRepository.deleteAll(existingIssueProfiles);

		List<Profile> assignees = projectValidator.projectMembersByIds(issue.getProject(), issueAssigneesReqDto.assignees());
		List<IssueProfile> newIssueProfiles = issueMapper.toIssueProfile(issue, assignees);
		issueProfileRepository.saveAll(newIssueProfiles);
	}

	@Transactional
	public void updateIssueLabels(long userId, String projectUrl, Long issueId, IssueLabelsReqDto issueLabelsReqDto) {
		projectValidator.validateAccess(userId, projectUrl);

		Issue issue = findIssueById(issueId);

		List<IssueLabel> existingIssueLabels = issueLabelRepository.findByIssueId(issueId);
		issueLabelRepository.deleteAll(existingIssueLabels);

		List<Label> labels = labelService.getLabelsByIds(issueLabelsReqDto.labels());
		List<IssueLabel> newIssueLabels = issueMapper.toIssueLabel(issue, labels);
		issueLabelRepository.saveAll(newIssueLabels);
	}

	@Transactional
	public void updateIssueKanbanConfig(long userId, String projectUrl, Long issueId, IssueKanbanConfigReqDto issueKanbanConfigReqDto) {
		ProjectAccessContext accessContext = projectValidator.validateAccess(userId, projectUrl);

		Issue issue = findIssueById(issueId);
		KanbanConfig fromKanbanConfig = issue.getKanbanConfig();
		KanbanConfig toKanbanConfig = kanbanConfigService.getKanbanConfigById(issueKanbanConfigReqDto.kanbanConfigId());
		issue.updateKanbanConfig(toKanbanConfig);

		eventPublisher.publishEvent(new IssueStatusChangedEvent(
			this,
			issueId,
			fromKanbanConfig.getId(),
			toKanbanConfig.getId(),
			userId,
			accessContext.project().getId()
		));
	}
}
