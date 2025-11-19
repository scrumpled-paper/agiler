package scrumpledpaper.agiler.kanban.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.kanban.dto.IssueStatusUpdateReqDto;
import scrumpledpaper.agiler.kanban.entity.Issue;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.kanban.repository.IssueRepository;
import scrumpledpaper.agiler.kanban.repository.IssueStatusHistoryRepository;
import scrumpledpaper.agiler.kanban.repository.KanbanConfigRepository;
import scrumpledpaper.agiler.notification.event.IssueStatusChangedEvent;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.repository.ProfileRepository;
import scrumpledpaper.agiler.project.service.ProjectValidator;

@Service
@RequiredArgsConstructor
public class KanbanService {

	private final ProjectValidator projectValidator;
	private final ApplicationEventPublisher eventPublisher;
	private final IssueRepository issueRepository;
	private final KanbanConfigRepository kanbanConfigRepository;

	@Transactional
	public void changeIssueStatus(long userId, long issueId, String projectUrl, IssueStatusUpdateReqDto request) {
		ProjectAccessContext accessContext = projectValidator.validateAccess(userId, projectUrl);

		Issue issue = issueRepository.findById(issueId)
				.orElseThrow(() -> new CustomException(ErrorCode.ISSUE_NOT_FOUND));
		KanbanConfig kanbanConfig = kanbanConfigRepository.findById(request.toKanbanConfigId())
				.orElseThrow(() -> new CustomException(ErrorCode.KANBAN_CONFIG_NOT_FOUND));
		issue.updateStatus(kanbanConfig);

		// 이벤트 발행
		eventPublisher.publishEvent(new IssueStatusChangedEvent(
				this,
				issueId,
				request.fromKanbanConfigId(),
				request.toKanbanConfigId(),
				userId,
				accessContext.project().getId()
		));
	}
}
