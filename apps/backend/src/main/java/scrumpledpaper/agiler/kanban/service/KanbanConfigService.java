package scrumpledpaper.agiler.kanban.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.kanban.dto.KanbanConfigResDto;
import scrumpledpaper.agiler.kanban.dto.KanbanConfigUpdateReqDto;
import scrumpledpaper.agiler.kanban.entity.DefaultKanbanConfig;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.kanban.mapper.KanbanConfigMapper;
import scrumpledpaper.agiler.kanban.repository.KanbanConfigRepository;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.service.ProjectValidator;

@Service
@RequiredArgsConstructor
public class KanbanConfigService {

	private static final int REQUIRED_BACKLOG_COUNT = 1;
	private static final int REQUIRED_DEFAULT_STATUS_COUNT = 1;
	private static final int REQUIRED_DONE_COUNT = 1;
	private static final int KANBAN_CONFIG_VERSION_START = 1;

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

	public List<KanbanConfig> updateKanbanConfigList(Project project, int version, KanbanConfigUpdateReqDto kanbanConfigUpdateReqDto) {
		kanbanConfigValidation(kanbanConfigUpdateReqDto);

		List<KanbanConfig> newConfigs = kanbanConfigUpdateReqDto.kanbanConfigs().stream()
			.map(dto -> kanbanConfigMapper.toEntity(project, dto, version))
			.toList();
		return kanbanConfigRepository.saveAll(newConfigs);
	}

	public void deleteAllKanbanConfigs(List<KanbanConfig> existingConfigs) {
		kanbanConfigRepository.deleteAll(existingConfigs);
	}

	private void kanbanConfigValidation(KanbanConfigUpdateReqDto kanbanConfigUpdateReqDto) {
		long defaultStatusCount = 0, backlogCount = 0, doneCount = 0;
		Set<Integer> prioritySet = new HashSet<>();
		Integer defaultPriority = null;
		Integer backlogPriority = null;
		Integer donePriority = null;

		for (KanbanConfigUpdateReqDto.KanbanConfigReqDto dto : kanbanConfigUpdateReqDto.kanbanConfigs()) {
			if (Boolean.TRUE.equals(dto.defaultStatus())) {
				defaultStatusCount++;
				defaultPriority = dto.priority();
			}
			if (Boolean.TRUE.equals(dto.backlog())) {
				backlogCount++;
				backlogPriority = dto.priority();
			}
			if (Boolean.TRUE.equals(dto.isDone())) {
				doneCount++;
				donePriority = dto.priority();
			}

			Integer priority = dto.priority();
			if (!prioritySet.add(priority)) {
				throw new CustomException(ErrorCode.DUPLICATE_KANBAN_CONFIG_PRIORITY);
			}
		}
		if (defaultStatusCount != REQUIRED_DEFAULT_STATUS_COUNT) {
			throw new CustomException(ErrorCode.INVALID_KANBAN_CONFIG_DEFAULT_STATUS);
		}
		if (backlogCount != REQUIRED_BACKLOG_COUNT) {
			throw new CustomException(ErrorCode.INVALID_KANBAN_CONFIG_BACKLOG_STATUS);
		}
		if (doneCount != REQUIRED_DONE_COUNT) {
			throw new CustomException(ErrorCode.INVALID_KANBAN_CONFIG_DONE_STATUS);
		}
		if (backlogPriority > defaultPriority || defaultPriority > donePriority) {
			throw new CustomException(ErrorCode.INVALID_KANBAN_CONFIG_PRIORITY_ORDER);
		}
	}

	public List<KanbanConfig> findAllByProject(Project project) {
		return kanbanConfigRepository.findAllByProjectId(project.getId());
	}


	@Transactional(readOnly = true)
	public List<KanbanConfigResDto> getKanbanConfigList(long userId, String projectUrl) {
		ProjectAccessContext projectAccessContext = projectValidator.validateAccess(userId, projectUrl);
		Project project = projectAccessContext.project();

		List<KanbanConfig> kanbanConfigs = kanbanConfigRepository.findByProjectIdOrderByPriorityAsc(project.getId());
		return kanbanConfigMapper.toDtoList(kanbanConfigs);
	}

	public KanbanConfig getBacklogKanbanConfig(Long projectId) {
		return kanbanConfigRepository.findByProjectIdAndBacklogTrue(projectId)
			.orElseThrow(() -> new CustomException(ErrorCode.KANBAN_CONFIG_NOT_FOUND));
	}

	public void createDefaultKanbanConfigs(Project savedProject) {
		kanbanConfigRepository.saveAll(
			Arrays.stream(DefaultKanbanConfig.values())
				.map(defaultKanbanConfig -> kanbanConfigMapper.toEntity(savedProject, defaultKanbanConfig, KANBAN_CONFIG_VERSION_START))
				.toList()
		);
	}
}
