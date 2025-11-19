package scrumpledpaper.agiler.kanban.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.kanban.repository.KanbanConfigRepository;

@Service
@RequiredArgsConstructor
public class KanbanConfigService {
	private final KanbanConfigRepository kanbanConfigRepository;

	public KanbanConfig getDefaultStatusKanbanConfig(Long projectId) {
		return kanbanConfigRepository.findByProjectIdAndDefaultStatusTrue(projectId)
			.orElseThrow(() -> new CustomException(ErrorCode.DEFAULT_KANBAN_CONFIG_NOT_FOUND));
	}
}
