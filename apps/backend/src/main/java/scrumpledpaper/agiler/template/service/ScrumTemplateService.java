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
import scrumpledpaper.agiler.template.dto.ScrumTemplateCreateReqDto;
import scrumpledpaper.agiler.template.dto.ScrumTemplateDetailResDto;
import scrumpledpaper.agiler.template.dto.ScrumTemplateResDto;
import scrumpledpaper.agiler.template.dto.ScrumTemplateUpdateReqDto;
import scrumpledpaper.agiler.template.entity.DefaultScrumTemplate;
import scrumpledpaper.agiler.template.entity.ScrumTemplate;
import scrumpledpaper.agiler.template.mapper.ScrumTemplateMapper;
import scrumpledpaper.agiler.template.repository.ScrumTemplateRepository;

@Service
@RequiredArgsConstructor
public class ScrumTemplateService {
	private final ScrumTemplateMapper scrumTemplateMapper;
	private final ScrumTemplateRepository scrumTemplateRepository;
	private final ProjectValidator projectValidator;

	public void createDefaultScrumTemplates(Project project) {
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

	@Transactional
	public void updateScrumTemplate(long userId, String projectUrl, ScrumTemplateUpdateReqDto scrumTemplateUpdateReqDto) {
		projectValidator.validateAccess(userId, projectUrl);

		ScrumTemplate scrumTemplate = findById(scrumTemplateUpdateReqDto.templateId());
		scrumTemplate.update(
			scrumTemplateUpdateReqDto.title(),
			scrumTemplateUpdateReqDto.description(),
			scrumTemplateUpdateReqDto.contents()
		);
	}

	public ScrumTemplate findById(Long id) {
		return scrumTemplateRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.SCRUM_TEMPLATE_NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public List<ScrumTemplateResDto> getScrumTemplateList(long userId, String projectUrl) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		List<ScrumTemplate> scrumTemplates = scrumTemplateRepository.findByProjectId(project.getId());
		return scrumTemplates.stream()
			.map(scrumTemplateMapper::toDto)
			.toList();
	}

	@Transactional(readOnly = true)
	public ScrumTemplateDetailResDto getScrumTemplate(long userId, String projectUrl, Long templateId) {
		projectValidator.validateAccess(userId, projectUrl);

		ScrumTemplate scrumTemplate = findById(templateId);
		return scrumTemplateMapper.toDetailDto(scrumTemplate);
	}

	@Transactional
	public void deleteScrumTemplate(long userId, String projectUrl, Long templateId) {
		projectValidator.validateAccess(userId, projectUrl);

		ScrumTemplate scrumTemplate = findById(templateId);
		scrumTemplateRepository.delete(scrumTemplate);
	}
}
