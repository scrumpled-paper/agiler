package scrumpledpaper.agiler.note.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scrumpledpaper.agiler.note.dto.NoteCreateReqDto;
import scrumpledpaper.agiler.note.dto.NoteCreateResDto;
import scrumpledpaper.agiler.note.repository.MeetingRespository;
import scrumpledpaper.agiler.note.repository.RetroRepository;
import scrumpledpaper.agiler.note.repository.ScrumRespository;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.service.ProjectValidator;

@Service
@RequiredArgsConstructor
public class NoteService {

	private final ProjectValidator projectValidator;
	private final ScrumRespository scrumRespository;
	private final MeetingRespository meetingRespository;
	private final RetroRepository retroRepository;

	@Transactional
	public NoteCreateResDto createNote(long userId, String projectUrl, String type) {
		ProjectAccessContext accessContext = projectValidator.validateAccess(userId, projectUrl);

		return switch (request.type()) {
			case "scrum" -> {
				var scrum = scrumRespository.save(request.toScrumEntity(accessContext.projectId()));
				yield new NoteCreateResDto(scrum.getId(), "scrum");
			}
			case "meeting" -> {
				var meeting = meetingRespository.save(request.toMeetingEntity(accessContext.projectId()));
				yield new NoteCreateResDto(meeting.getId(), "meeting");
			}
			case "retro" -> {
				var retro = retroRepository.save(request.toRetroEntity(accessContext.projectId()));
				yield new NoteCreateResDto(retro.getId(), "retro");
			}
			default -> throw new IllegalArgumentException("Invalid note type: " + request.type());
		};

	}

	private boolean


}
