package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.project.entity.Project;

public class KanbanConfigFixture {
	public static KanbanConfig createKanbanConfig(Project project, String statusName, int priority, boolean defaultStatus, boolean backlog, Boolean isDone) {
		return KanbanConfig.builder()
			.project(project)
			.statusName(statusName)
			.priority(priority)
			.defaultStatus(defaultStatus)
			.backlog(backlog)
			.isDone(isDone)
			.build();
	}
}
