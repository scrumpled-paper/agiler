package scrumpledpaper.agiler.template.service;

import java.util.Arrays;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.service.ProjectValidator;
import scrumpledpaper.agiler.template.dto.IssueTemplateCreateReqDto;
import scrumpledpaper.agiler.template.entity.DefaultIssueTemplate;
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
