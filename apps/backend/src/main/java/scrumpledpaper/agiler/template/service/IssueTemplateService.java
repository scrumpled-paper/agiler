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
import scrumpledpaper.agiler.template.dto.IssueTemplateCreateReqDto;
import scrumpledpaper.agiler.template.dto.IssueTemplateListResDto;
import scrumpledpaper.agiler.template.dto.IssueTemplateResDto;
import scrumpledpaper.agiler.template.dto.IssueTemplateUpdateReqDto;
import scrumpledpaper.agiler.template.entity.DefaultIssueTemplate;
import scrumpledpaper.agiler.template.entity.IssueTemplate;
import scrumpledpaper.agiler.template.mapper.IssueTemplateMapper;
import scrumpledpaper.agiler.template.repository.IssueTemplateRepository;

@Service
@RequiredArgsConstructor
public class IssueTemplateService {
	private final IssueTemplateMapper issueTemplateMapper;
	private final IssueTemplateRepository issueTemplateRepository;
	private final ProjectValidator projectValidator;

	public void createDefaultIssueTemplates(Project project) {
		issueTemplateRepository.saveAll(
			Arrays.stream(DefaultIssueTemplate.values())
				.map(defaultIssueTemplate -> issueTemplateMapper.toEntity(project, defaultIssueTemplate))
				.toList()
		);
	}

	@Transactional
	public void createIssueTemplate(long userId, String projectUrl, IssueTemplateCreateReqDto issueTemplateCreateReqDto) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		issueTemplateRepository.save(
			issueTemplateMapper.toEntity(project, issueTemplateCreateReqDto)
		);
	}

	@Transactional
	public void updateIssueTemplate(long userId, String projectUrl, IssueTemplateUpdateReqDto issueTemplateCreateReqDto) {
		projectValidator.validateAccess(userId, projectUrl);

		IssueTemplate issueTemplate = findById(issueTemplateCreateReqDto.templateId());
		issueTemplate.update(
			issueTemplateCreateReqDto.title(),
			issueTemplateCreateReqDto.description(),
			issueTemplateCreateReqDto.contents()
		);
	}

	private IssueTemplate findById(Long id) {
		return issueTemplateRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.ISSUE_TEMPLATE_NOT_FOUND));
	}

	public List<IssueTemplateResDto> getIssueTemplateList(long userId, String projectUrl) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		List<IssueTemplate> issueTemplates = issueTemplateRepository.findAllByProjectId(project.getId());
		return issueTemplates.stream()
			.map(issueTemplateMapper::toDto)
			.toList();
	}
}
