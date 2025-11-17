package scrumpledpaper.agiler.template.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.service.ProjectValidator;
import scrumpledpaper.agiler.template.dto.MeetingTemplateCreateReqDto;
import scrumpledpaper.agiler.template.dto.MeetingTemplateDetailResDto;
import scrumpledpaper.agiler.template.dto.MeetingTemplateResDto;
import scrumpledpaper.agiler.template.dto.MeetingTemplateUpdateReqDto;
import scrumpledpaper.agiler.template.entity.DefaultMeetingTemplate;
import scrumpledpaper.agiler.template.entity.MeetingTemplate;
import scrumpledpaper.agiler.template.mapper.MeetingTemplateMapper;
import scrumpledpaper.agiler.template.repository.MeetingTemplateRepository;

@Service
@RequiredArgsConstructor
public class MeetingTemplateService {
	private final MeetingTemplateMapper meetingTemplateMapper;
	private final MeetingTemplateRepository meetingTemplateRepository;
	private final ProjectValidator projectValidator;

	public void createDefaultMeetingTemplates(Project project) {
		meetingTemplateRepository.saveAll(
			Arrays.stream(DefaultMeetingTemplate.values())
				.map(defaultMeetingTemplate -> meetingTemplateMapper.toEntity(project, defaultMeetingTemplate))
				.toList()
		);
	}

	@Transactional
	public void createMeetingTemplate(long userId, String projectUrl, MeetingTemplateCreateReqDto meetingTemplateCreateReqDto) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		meetingTemplateRepository.save(
			meetingTemplateMapper.toEntity(project, meetingTemplateCreateReqDto)
		);
	}

	@Transactional
	public void updateMeetingTemplate(long userId, String projectUrl, MeetingTemplateUpdateReqDto meetingTemplateUpdateReqDto) {
		projectValidator.validateAccess(userId, projectUrl);

		MeetingTemplate meetingTemplate = findById(meetingTemplateUpdateReqDto.templateId());
		meetingTemplate.update(
			meetingTemplateUpdateReqDto.title(),
			meetingTemplateUpdateReqDto.description(),
			meetingTemplateUpdateReqDto.contents()
		);
	}

	private MeetingTemplate findById(Long id) {
		return meetingTemplateRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.MEETING_TEMPLATE_NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public List<MeetingTemplateResDto> getMeetingTemplateList(long userId, String projectUrl) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		List<MeetingTemplate> meetingTemplates = meetingTemplateRepository.findByProjectId(project.getId());
		return meetingTemplates.stream()
			.map(meetingTemplateMapper::toDto)
			.toList();
	}

	@Transactional(readOnly = true)
	public MeetingTemplateDetailResDto getMeetingTemplate(long userId, String projectUrl, Long templateId) {
		projectValidator.validateAccess(userId, projectUrl);

		MeetingTemplate meetingTemplate = findById(templateId);
		return meetingTemplateMapper.toDetailDto(meetingTemplate);
	}

	@Transactional
	public void deleteMeetingTemplate(long userId, String projectUrl, Long templateId) {
		projectValidator.validateAccess(userId, projectUrl);

		MeetingTemplate meetingTemplate = findById(templateId);
		meetingTemplateRepository.delete(meetingTemplate);
	}
}

