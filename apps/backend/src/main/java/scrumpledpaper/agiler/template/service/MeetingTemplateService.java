package scrumpledpaper.agiler.template.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.template.entity.DefaultMeetingTemplate;
import scrumpledpaper.agiler.template.entity.MeetingTemplate;
import scrumpledpaper.agiler.template.mapper.MeetingTemplateMapper;
import scrumpledpaper.agiler.template.repository.MeetingTemplateRepository;
@Service
@RequiredArgsConstructor
public class MeetingTemplateService {
	private final MeetingTemplateMapper meetingTemplateMapper;
	private final MeetingTemplateRepository meetingTemplateRepository;

	public void createDefaultMeetingTemplates(Project project) {
		meetingTemplateRepository.saveAll(
			Arrays.stream(DefaultMeetingTemplate.values())
				.map(defaultMeetingTemplate -> meetingTemplateMapper.toEntity(project, defaultMeetingTemplate))
				.toList()
		);
	}

