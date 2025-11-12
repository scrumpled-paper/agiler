package scrumpledpaper.agiler.kanban.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.kanban.dto.LabelCreateReqDto;
import scrumpledpaper.agiler.kanban.dto.LabelResDto;
import scrumpledpaper.agiler.kanban.entity.DefaultLabel;
import scrumpledpaper.agiler.kanban.entity.Label;
import scrumpledpaper.agiler.kanban.mapper.LabelMapper;
import scrumpledpaper.agiler.kanban.repository.LabelRepository;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.service.ProjectValidator;

@Service
@RequiredArgsConstructor
public class LabelService {
	private final LabelMapper labelMapper;
	private final LabelRepository labelRepository;
	private final ProjectValidator projectValidator;

	public void createDefaultLabels(Project project) {
		labelRepository.saveAll(
			Arrays.stream(DefaultLabel.values())
				.map(defaultLabel -> labelMapper.toEntity(project, defaultLabel))
				.toList()
		);
	}

	@Transactional
	public void createLabel(long userId, String projectUrl, LabelCreateReqDto labelCreateReqDto) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		Label label = labelMapper.toEntity(project, labelCreateReqDto);
		labelRepository.save(label);
	}

	public List<LabelResDto> getLabelList(long userId, String projectUrl) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		List<Label> labels = labelRepository.findByProjectId(project.getId());
		return labels.stream()
			.map(labelMapper::toDto)
			.toList();
	}
}
