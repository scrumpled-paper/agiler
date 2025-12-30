package scrumpledpaper.agiler.note.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.note.dto.internal.NoteDataResDto;
import scrumpledpaper.agiler.note.dto.internal.NotePermissionResDto;
import scrumpledpaper.agiler.note.entity.Meeting;
import scrumpledpaper.agiler.note.entity.Retro;
import scrumpledpaper.agiler.note.entity.Scrum;
import scrumpledpaper.agiler.note.repository.MeetingRespository;
import scrumpledpaper.agiler.note.repository.RetroRepository;
import scrumpledpaper.agiler.note.repository.ScrumRespository;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.service.ProjectValidator;

@Service
@RequiredArgsConstructor
public class InternalNoteService {

	private final ProjectValidator projectValidator;
	private final RetroRepository retroRepository;
	private final ScrumRespository scrumRepository;
	private final MeetingRespository meetingRepository;

	private static final String TYPE_RETRO = "retro";
	private static final String TYPE_SCRUM = "scrum";
	private static final String TYPE_MEETING = "meeting";

	@Transactional(readOnly = true)
	public NoteDataResDto getNote(String type, Long id) {
		return switch (type) {
			case TYPE_RETRO -> {
				Retro retro = retroRepository.findById(id)
					.orElseThrow(() -> new CustomException(ErrorCode.RETRO_NOT_FOUND));
				yield new NoteDataResDto(retro.getId(), TYPE_RETRO, retro.getTitle(), retro.getContents());
			}
			case TYPE_SCRUM -> {
				Scrum scrum = scrumRepository.findById(id)
					.orElseThrow(() -> new CustomException(ErrorCode.SCRUM_NOT_FOUND));
				yield new NoteDataResDto(scrum.getId(), TYPE_SCRUM, scrum.getTitle(), scrum.getContents());
			}
			case TYPE_MEETING -> {
				Meeting meeting = meetingRepository.findById(id)
					.orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND));
				yield new NoteDataResDto(meeting.getId(), TYPE_MEETING, meeting.getTitle(), meeting.getContents());
			}
			default -> throw new CustomException(ErrorCode.INVALID_NOTE_TYPE);
		};
	}

	@Transactional
	public void updateNoteContents(String type, Long id, String contents) {
		switch (type) {
			case TYPE_RETRO -> {
				Retro retro = retroRepository.findById(id)
					.orElseThrow(() -> new CustomException(ErrorCode.RETRO_NOT_FOUND));
				retro.updateContents(contents);
			}
			case TYPE_SCRUM -> {
				Scrum scrum = scrumRepository.findById(id)
					.orElseThrow(() -> new CustomException(ErrorCode.SCRUM_NOT_FOUND));
				scrum.updateContents(contents);
			}
			case TYPE_MEETING -> {
				Meeting meeting = meetingRepository.findById(id)
					.orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND));
				meeting.updateContents(contents);
			}
			default -> throw new CustomException(ErrorCode.INVALID_NOTE_TYPE);
		}
	}

	@Transactional(readOnly = true)
	public NotePermissionResDto checkPermission(String type, Long id, Long userId) {
		ProjectAccessContext accessContext = projectValidator.validateAccess()

		Long projectId = getProjectIdByNote(type, id);
		boolean editable = profileRepository.findByUserIdAndProjectId(userId, projectId).isPresent();
		return new NotePermissionResDto(editable);
	}

	private long getProjectIdByNote(String type, Long id) {
		return switch (type) {
			case TYPE_RETRO -> retroRepository.findById(id)
				.orElseThrow(() -> new CustomException(ErrorCode.RETRO_NOT_FOUND))
				.getProject().getId();
			case TYPE_SCRUM -> scrumRepository.findById(id)
				.orElseThrow(() -> new CustomException(ErrorCode.SCRUM_NOT_FOUND))
				.getProject().getId();
			case TYPE_MEETING -> meetingRepository.findById(id)
				.orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND))
				.getProject().getId();
			default -> throw new CustomException(ErrorCode.INVALID_NOTE_TYPE);
		};
	}
}