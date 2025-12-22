package scrumpledpaper.agiler.note.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.common.utils.WssTokenProvider;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.service.ProjectValidator;

@Service
@RequiredArgsConstructor
public class WssService {
	private final ProjectValidator projectValidator;
	private final MeetingService meetingService;
	private final RetroService retroService;
	private final ScrumService scrumService;
	private final WssTokenProvider wssTokenProvider;

	@Transactional(readOnly = true)
	public String generateWssToken(Long userId, String projectUrl, String docId) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		checkDocumentInProject(project, docId);
		return wssTokenProvider.createWssToken(userId, docId);
	}

	private void checkDocumentInProject(Project project, String docId) {
		String[] parts = docId.split("-", 2);
		if (parts.length != 2) {
			throw new CustomException(ErrorCode.INVALID_DOCUMENT_ID);
		}

		String prefix = parts[0];
		String numStr = parts[1];

		try {
			Long id = Long.parseLong(numStr);

			switch (prefix) {
				case "meeting" -> meetingService.validateMeetingInProject(project.getId(), id);
				case "retro" -> retroService.validateRetroInProject(project.getId(), id);
				case "scrum" -> scrumService.validateScrumInProject(project.getId(), id);
				default -> throw new CustomException(ErrorCode.INVALID_DOCUMENT_ID);
			}
		} catch (NumberFormatException e) {
			throw new CustomException(ErrorCode.INVALID_DOCUMENT_ID);
		}
	}
}
