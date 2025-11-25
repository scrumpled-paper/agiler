package scrumpledpaper.agiler.kanban.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.kanban.dto.KanbanConfigUpdateReqDto;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.kanban.mapper.KanbanConfigMapper;
import scrumpledpaper.agiler.kanban.repository.KanbanConfigRepository;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.service.ProjectValidator;

@Service
@RequiredArgsConstructor
public class KanbanConfigService {
	private final ProjectValidator projectValidator;
	private final KanbanConfigRepository kanbanConfigRepository;
	private final KanbanConfigMapper kanbanConfigMapper;

	public KanbanConfig getDefaultStatusKanbanConfig(Long projectId) {
		return kanbanConfigRepository.findByProjectIdAndDefaultStatusTrue(projectId)
			.orElseThrow(() -> new CustomException(ErrorCode.DEFAULT_KANBAN_CONFIG_NOT_FOUND));
	}

	public KanbanConfig getKanbanConfigById(Long kanbanConfigId) {
		return kanbanConfigRepository.findById(kanbanConfigId)
			.orElseThrow(() -> new CustomException(ErrorCode.KANBAN_CONFIG_NOT_FOUND));
	}

	public void updateKanbanConfig(long userId, String projectUrl, KanbanConfigUpdateReqDto kanbanConfigUpdateReqDto) {
		ProjectAccessContext projectAccessContext = projectValidator.validateAccess(userId, projectUrl);
		Project project = projectAccessContext.project();

		kanbanConfigValidation(kanbanConfigUpdateReqDto);

		List<KanbanConfig> existingConfigs = kanbanConfigRepository.findByProjectId(project.getId());
		kanbanConfigRepository.deleteAll(existingConfigs);

		List<KanbanConfig> newConfigs = kanbanConfigUpdateReqDto.kanbanConfigs().stream()
			.map(dto -> kanbanConfigMapper.toEntity(project, dto))
			.toList();
		kanbanConfigRepository.saveAll(newConfigs);
	}

	private void kanbanConfigValidation(KanbanConfigUpdateReqDto kanbanConfigUpdateReqDto) {
		long defaultStatusCount = 0, backlogCount = 0, doneCount = 0;
		Set<Integer> prioritySet = new HashSet<>();

		for (KanbanConfigUpdateReqDto.KanbanConfigReqDto dto : kanbanConfigUpdateReqDto.kanbanConfigs()) {
			if (Boolean.TRUE.equals(dto.defaultStatus()))
				defaultStatusCount++;
			if (Boolean.TRUE.equals(dto.backlog()))
				backlogCount++;
			if (Boolean.TRUE.equals(dto.isDone()))
				doneCount++;

			Integer priority = dto.priority();
			if (!prioritySet.add(priority)) {
				throw new CustomException(ErrorCode.DUPLICATE_KANBAN_CONFIG_PRIORITY);
			}
		}

		if (defaultStatusCount != 1) {
			throw new CustomException(ErrorCode.INVALID_KANBAN_CONFIG_DEFAULT_STATUS);
		}
		if (backlogCount != 1) {
			throw new CustomException(ErrorCode.INVALID_KANBAN_CONFIG_BACKLOG_STATUS);
		}
		if (doneCount != 1) {
			throw new CustomException(ErrorCode.INVALID_KANBAN_CONFIG_DONE_STATUS);
		}
	}
}
