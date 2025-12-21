package scrumpledpaper.agiler.note.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.PageResDto;
import scrumpledpaper.agiler.common.PageValidator;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.image.service.ImageService;
import scrumpledpaper.agiler.note.dto.MeetingResDto;
import scrumpledpaper.agiler.note.dto.NoteCreateReqDto;
import scrumpledpaper.agiler.note.dto.NoteDeleteReqDto;
import scrumpledpaper.agiler.note.entity.Meeting;
import scrumpledpaper.agiler.note.entity.MeetingProfile;
import scrumpledpaper.agiler.note.mapper.MeetingMapper;
import scrumpledpaper.agiler.note.repository.MeetingProfileRepository;
import scrumpledpaper.agiler.note.repository.MeetingRepository;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.service.ProjectValidator;
import scrumpledpaper.agiler.template.entity.MeetingTemplate;
import scrumpledpaper.agiler.template.service.MeetingTemplateService;

@Service
@RequiredArgsConstructor
public class MeetingService {
	private final ImageService imageService;
	private final MeetingRepository meetingRepository;
	private final MeetingTemplateService meetingTemplateService;
	private final MeetingProfileRepository meetingProfileRepository;
	private final MeetingMapper meetingMapper;
	private final ProjectValidator projectValidator;

	@Transactional(readOnly = true)
	public PageResDto<MeetingResDto> getMeetings(long userId, String projectUrl, Pageable pageable) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		Page<Meeting> meetingPage = meetingRepository.findAllByProjectId(project.getId(), pageable);
		PageValidator.validatePageInRange(meetingPage);

		if (meetingPage.isEmpty()) {
			return PageResDto.empty(meetingPage);
		}

		List<Long> meetingIds = meetingPage.stream()
			.map(Meeting::getId)
			.toList();

		List<MeetingProfile> meetingProfiles = meetingProfileRepository.findAllByMeetingIdsWithProfile(meetingIds);

		List<Long> imageIds = meetingProfiles.stream()
			.map(MeetingProfile::getProfile)
			.map(Profile::getImageId)
			.distinct()
			.toList();  

		Map<Long, String> imageUrls = imageService.getImageUrlsByIds(imageIds);

		Map<Long, List<Profile>> participantMap = meetingProfiles
			.stream()
			.collect(Collectors.groupingBy(
				mp -> mp.getMeeting().getId(),
				Collectors.mapping(MeetingProfile::getProfile, Collectors.toList())
			));

		Page<MeetingResDto> dtoPage = meetingPage.map(
			meeting -> meetingMapper.toMeetingResDto(
				meeting,
				participantMap.getOrDefault(meeting.getId(), List.of()),
				imageUrls
			)
		);

		return PageResDto.from(dtoPage);
	}

	@Transactional
	public long createMeeting(long userId, String projectUrl, NoteCreateReqDto noteCreateReqDto) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		Meeting meeting = createMeetingEntity(project, noteCreateReqDto);
		meetingRepository.save(meeting);

		return meeting.getId();
	}

	private Meeting createMeetingEntity(Project project, NoteCreateReqDto noteCreateReqDto) {
		if (noteCreateReqDto.templateId() != null) {
			MeetingTemplate template = meetingTemplateService.findById(noteCreateReqDto.templateId());
			return meetingMapper.toEntity(project, template);
		}
		return meetingMapper.toEntity(project, "", "");
	}

	@Transactional
	public void deleteMeetings(long userId, String projectUrl, NoteDeleteReqDto noteDeleteReqDto) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		Meeting meeting = findByIdAndProject(noteDeleteReqDto.id(), project);
		List<MeetingProfile> meetingProfiles = meetingProfileRepository.findAllByMeetingId(meeting.getId());

		meetingProfileRepository.deleteAll(meetingProfiles);
		meetingRepository.delete(meeting);
	}

	private Meeting findByIdAndProject(Long id, Project project) {
		return meetingRepository.findByIdAndProjectId(id, project.getId())
			.orElseThrow(() -> new CustomException(ErrorCode.NOTE_NOT_FOUND));
	}
}
