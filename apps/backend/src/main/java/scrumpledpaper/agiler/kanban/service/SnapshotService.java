package scrumpledpaper.agiler.kanban.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.kanban.entity.KanbanConfigSnapshot;
import scrumpledpaper.agiler.kanban.mapper.KanbanConfigMapper;
import scrumpledpaper.agiler.kanban.repository.KanbanConfigSnapshotRepository;

@Service
@RequiredArgsConstructor
public class SnapshotService {
	private final KanbanConfigSnapshotRepository kanbanConfigSnapshotRepository;
	private final KanbanConfigMapper kanbanConfigMapper;

	private static final int ISSUE_SNAPSHOT_START_HOUR = 6;


	public void kanbanConfigSnapshot(List<KanbanConfig> kanbanConfigs) {
		List<KanbanConfigSnapshot> snapshots = kanbanConfigs.stream()
			.map(kanbanConfigMapper::toKanbanConfigSnapshot)
			.toList();

		kanbanConfigSnapshotRepository.saveAll(snapshots);
	}
}
