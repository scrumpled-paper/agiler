package scrumpledpaper.agiler.template.service;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.template.entity.DefaultScrumTemplate;
import scrumpledpaper.agiler.template.mapper.ScrumTemplateMapper;
import scrumpledpaper.agiler.template.repository.ScrumTemplateRepository;

@Service
@RequiredArgsConstructor
public class ScrumTemplateService {
	private final ScrumTemplateMapper scrumTemplateMapper;
	private final ScrumTemplateRepository scrumTemplateRepository;

	public void createDefaultIssueTemplates(Project project) {
		scrumTemplateRepository.saveAll(
			Arrays.stream(DefaultScrumTemplate.values())
				.map(defaultScrumTemplate -> scrumTemplateMapper.toEntity(project, defaultScrumTemplate))
				.toList()
		);
	}
}
