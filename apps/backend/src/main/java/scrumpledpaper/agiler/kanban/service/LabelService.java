package scrumpledpaper.agiler.kanban.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.kanban.entity.DefaultLabel;
import scrumpledpaper.agiler.kanban.entity.Label;
import scrumpledpaper.agiler.kanban.repository.LabelRepository;
import scrumpledpaper.agiler.project.entity.Project;

@Service
@RequiredArgsConstructor
public class LabelService {
	private final LabelRepository labelRepository;

	public void createDefaultLabels(Project project) {
		List<Label> defaultLabels = Arrays.stream(DefaultLabel.values())
			.map(defaultLabel -> createLabel(project, defaultLabel))
			.toList();

		labelRepository.saveAll(defaultLabels);
	}

	private Label createLabel(Project project, DefaultLabel defaultLabel) {
		return Label.builder()
			.project(project)
			.name(defaultLabel.getName())
			.color(defaultLabel.getColor())
			.description(defaultLabel.getDescription())
			.build();
	}
}
