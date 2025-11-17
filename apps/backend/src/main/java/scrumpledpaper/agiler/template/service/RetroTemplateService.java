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
import scrumpledpaper.agiler.template.dto.RetroTemplateCreateReqDto;
import scrumpledpaper.agiler.template.dto.RetroTemplateResDto;
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

	@Transactional
	public void updateRetroTemplate(long userId, String projectUrl, RetroTemplateUpdateReqDto retroTemplateUpdateReqDto) {
		projectValidator.validateAccess(userId, projectUrl);

		RetroTemplate retroTemplate = findById(retroTemplateUpdateReqDto.templateId());
		retroTemplate.update(
			retroTemplateUpdateReqDto.title(),
			retroTemplateUpdateReqDto.description(),
			retroTemplateUpdateReqDto.contents()
		);
	}

	private RetroTemplate findById(Long id) {
		return retroTemplateRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.RETRO_TEMPLATE_NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public List<RetroTemplateResDto> getRetroTemplateList(long userId, String projectUrl) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		List<RetroTemplate> retroTemplates = retroTemplateRepository.findByProjectId(project.getId());
		return retroTemplates.stream()
			.map(retroTemplateMapper::toDto)
			.toList();
	}

}

