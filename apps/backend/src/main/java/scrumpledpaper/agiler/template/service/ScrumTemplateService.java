package scrumpledpaper.agiler.template.service;

import java.util.Arrays;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.service.ProjectValidator;
import scrumpledpaper.agiler.template.dto.ScrumTemplateCreateReqDto;
import scrumpledpaper.agiler.template.entity.DefaultScrumTemplate;
import scrumpledpaper.agiler.template.mapper.ScrumTemplateMapper;
import scrumpledpaper.agiler.template.repository.ScrumTemplateRepository;

@Service
@RequiredArgsConstructor
public class ScrumTemplateService {
	private final ScrumTemplateMapper scrumTemplateMapper;
	private final ScrumTemplateRepository scrumTemplateRepository;
	private final ProjectValidator projectValidator;

	public void createDefaultIssueTemplates(Project project) {
		scrumTemplateRepository.saveAll(
			Arrays.stream(DefaultScrumTemplate.values())
				.map(defaultScrumTemplate -> scrumTemplateMapper.toEntity(project, defaultScrumTemplate))
				.toList()
		);
	}

	@Transactional
	public void createScrumTemplate(long userId, String projectUrl, ScrumTemplateCreateReqDto scrumTemplateCreateReqDto) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		scrumTemplateRepository.save(
			scrumTemplateMapper.toEntity(project, scrumTemplateCreateReqDto)
		);
	}
}
