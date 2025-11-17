package scrumpledpaper.agiler.template.service;


import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.template.entity.RetroTemplate;
import scrumpledpaper.agiler.template.mapper.RetroTemplateMapper;
import scrumpledpaper.agiler.template.repository.RetroTemplateRepository;
@Service
@RequiredArgsConstructor
public class RetroTemplateService {
	private final RetroTemplateMapper retroTemplateMapper;
	private final RetroTemplateRepository retroTemplateRepository;

	public void createDefaultRetroTemplates(Project project) {
		retroTemplateRepository.saveAll(
			Arrays.stream(DefaultRetroTemplate.values())
				.map(defaultRetroTemplate -> retroTemplateMapper.toEntity(project, defaultRetroTemplate))
				.toList()
		);
	}

}

