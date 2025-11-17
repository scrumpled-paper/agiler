package scrumpledpaper.agiler.template.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.service.ProjectValidator;
import scrumpledpaper.agiler.template.dto.RetroTemplateCreateReqDto;
import scrumpledpaper.agiler.template.entity.DefaultRetroTemplate;
import scrumpledpaper.agiler.template.entity.RetroTemplate;
import scrumpledpaper.agiler.template.mapper.RetroTemplateMapper;
import scrumpledpaper.agiler.template.repository.RetroTemplateRepository;
@Service
@RequiredArgsConstructor
public class RetroTemplateService {
	private final RetroTemplateMapper retroTemplateMapper;
	private final RetroTemplateRepository retroTemplateRepository;
	private final ProjectValidator projectValidator;

	public void createDefaultRetroTemplates(Project project) {
		retroTemplateRepository.saveAll(
			Arrays.stream(DefaultRetroTemplate.values())
				.map(defaultRetroTemplate -> retroTemplateMapper.toEntity(project, defaultRetroTemplate))
				.toList()
		);
	}

	@Transactional
	public void createRetroTemplate(long userId, String projectUrl, RetroTemplateCreateReqDto retroTemplateCreateReqDto) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		retroTemplateRepository.save(
			retroTemplateMapper.toEntity(project, retroTemplateCreateReqDto)
		);
	}

}

