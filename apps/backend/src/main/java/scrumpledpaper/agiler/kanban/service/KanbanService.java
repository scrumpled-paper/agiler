package scrumpledpaper.agiler.kanban.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.kanban.dto.IssueStatusUpdateReqDto;
import scrumpledpaper.agiler.kanban.entity.Issue;
import scrumpledpaper.agiler.kanban.entity.IssueStatusHistory;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.kanban.repository.IssueRepository;
import scrumpledpaper.agiler.kanban.repository.IssueStatusHistoryRepository;
import scrumpledpaper.agiler.kanban.repository.KanbanConfigRepository;
import scrumpledpaper.agiler.notification.event.IssueStatusChangedEvent;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.repository.ProfileRepository;
import scrumpledpaper.agiler.project.service.ProjectValidator;

@Service
@RequiredArgsConstructor
public class KanbanService {

	private final ProjectValidator projectValidator;
	private final ApplicationEventPublisher eventPublisher;
	private final IssueRepository issueRepository;
	private final KanbanConfigRepository kanbanConfigRepository;
	private final IssueStatusHistoryRepository issueStatusHistoryRepository;
	private final ProfileRepository profileRepository;

	@Transactional
	public void changeIssueStatus(long userId, long issueId, String projectUrl, IssueStatusUpdateReqDto request) {
		projectValidator.validateAccess(userId, projectUrl);

		Issue issue = issueRepository.findById(issueId)
				.orElseThrow(() -> new CustomException(ErrorCode.ISSUE_NOT_FOUND));
		KanbanConfig oldKanbanConfig = issue.getKanbanConfig();
		KanbanConfig newKanbanConfig = new KanbanConfig(oldKanbanConfig);
		newKanbanConfig.updateStatusName(request.newStatus());
		KanbanConfig savedNewKanbanConfig = kanbanConfigRepository.save(newKanbanConfig);

		Long projectId = oldKanbanConfig.getProject().getId();
		Profile updaterProfile = profileRepository.findByUserIdAndProjectId(userId, projectId)
				.orElseThrow(() -> new CustomException(ErrorCode.PROJECT_PROFILE_NOT_FOUND));

		issue.updateStatus(savedNewKanbanConfig);

		IssueStatusHistory history = new IssueStatusHistory(issue, updaterProfile, oldKanbanConfig, savedNewKanbanConfig);
		issueStatusHistoryRepository.save(history);

		// 이벤트 발행
		eventPublisher.publishEvent(new IssueStatusChangedEvent(
				this,
				issueId,
				oldKanbanConfig.getStatusName(),
				savedNewKanbanConfig.getStatusName(),
				userId,
				projectId
		));
	}
}
