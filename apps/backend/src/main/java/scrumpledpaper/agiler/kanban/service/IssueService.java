package scrumpledpaper.agiler.kanban.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.kanban.dto.IssueCreateReqDto;
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
}
