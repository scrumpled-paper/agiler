package scrumpledpaper.agiler.kanban.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.kanban.dto.LabelCreateReqDto;
import scrumpledpaper.agiler.kanban.dto.LabelResDto;
import scrumpledpaper.agiler.kanban.dto.LabelUpdateReqDto;
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

	@Transactional
	public void updateLabel(long userId, String projectUrl, Long labelId, LabelUpdateReqDto labelUpdateReqDto) {
		projectValidator.validateAccess(userId, projectUrl);

		Label label = findLabelById(labelId);
		label.update(labelUpdateReqDto.name(), labelUpdateReqDto.color(), labelUpdateReqDto.description());
	}

	public Label findLabelById(Long labelId) {
		return labelRepository.findById(labelId).orElseThrow(() -> new CustomException(ErrorCode.LABEL_NOT_FOUND));
	}

	@Transactional
	public void deleteLabels(long userId, String projectUrl, long id) {
		projectValidator.validateAccess(userId, projectUrl);

		Label label = findLabelById(id);
		labelRepository.delete(label);
	}

	public List<Label> getLabelsByIds(List<Long> labels) {
		return labelRepository.findAllById(labels);
	}
}
