package scrumpledpaper.agiler.fixture;

import static scrumpledpaper.agiler.common.TestDataFactory.*;

import java.util.ArrayList;
import java.util.List;

import scrumpledpaper.agiler.kanban.dto.KanbanConfigUpdateReqDto;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.project.entity.Project;

public class KanbanConfigFixture {
	public static KanbanConfig create(Project project, String statusName, int priority, boolean defaultStatus, boolean backlog, Boolean isDone) {
		return KanbanConfig.builder()
			.project(project)
			.statusName(statusName)
			.priority(priority)
			.defaultStatus(defaultStatus)
			.backlog(backlog)
			.isDone(isDone)
			.build();
	}

	public static KanbanConfigUpdateReqDto createUpdateReqDto(int count) {
		List<KanbanConfigUpdateReqDto.KanbanConfigReqDto> configs = new ArrayList<>();

		for (int i = 1; i <= count; i++) {
			boolean backlog = (i == 1);
			boolean defaultStatus = (i == 2);
			boolean isDone = (i == 3);

			KanbanConfigUpdateReqDto.KanbanConfigReqDto kanbanConfigReqDto =
				new KanbanConfigUpdateReqDto.KanbanConfigReqDto(
					randomString(5),
					i,
					defaultStatus,
					backlog,
					isDone
				);
			configs.add(kanbanConfigReqDto);
		}
		return new KanbanConfigUpdateReqDto(configs);
	}
}
