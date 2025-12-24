package scrumpledpaper.agiler.note.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.common.utils.WssTokenProvider;
import scrumpledpaper.agiler.note.dto.RetroDetailResDto;
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

	private record ParsedDocId(String prefix, Long id) {}

	@Transactional(readOnly = true)
	public String generateWssToken(Long userId, String projectUrl, String docId) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		checkDocumentInProject(project, docId);
		return wssTokenProvider.createWssToken(userId, docId);
	}


	private void checkDocumentInProject(Project project, String docId) {
		ParsedDocId parsed = parseDocId(docId);

		switch (parsed.prefix()) {
			case "meeting" -> meetingService.validateMeetingInProject(project.getId(), parsed.id());
			case "retro" -> retroService.validateRetroInProject(project.getId(), parsed.id());
			case "scrum" -> scrumService.validateScrumInProject(project.getId(), parsed.id());
			default -> throw new CustomException(ErrorCode.INVALID_DOCUMENT_ID);
		}
	}

	private ParsedDocId parseDocId(String docId) {
		String[] parts = docId.split("-", 2);
		if (parts.length != 2) {
			throw new CustomException(ErrorCode.INVALID_DOCUMENT_ID);
		}

		try {
			Long id = Long.parseLong(parts[1]);
			return new ParsedDocId(parts[0], id);
		} catch (NumberFormatException e) {
			throw new CustomException(ErrorCode.INVALID_DOCUMENT_ID);
		}
	}

	@Transactional(readOnly = true)
	public RetroDetailResDto getRetroDetail(long id) {
		return retroService.getRetroDetail(id);
	}

}
