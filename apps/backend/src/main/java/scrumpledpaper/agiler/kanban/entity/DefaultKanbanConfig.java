package scrumpledpaper.agiler.kanban.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DefaultKanbanConfig {
	BACKLOG("Backlog", "Tasks that are yet to be started", 1, true, false, false),
	TO_DO("To Do", "Tasks that need to be started", 2, false, true, false),
	IN_PROGRESS("In Progress", "Tasks that are currently being worked on", 3, false, false, false),
	DONE("Done", "Completed tasks", 4, false, false, true);


	private final String statusName;
	private final String description;
	private final int priority;
	private final boolean backlog;
	private final boolean defaultStatus;
	private final boolean done;
}
