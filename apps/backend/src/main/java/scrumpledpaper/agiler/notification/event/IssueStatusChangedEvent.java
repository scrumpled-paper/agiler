package scrumpledpaper.agiler.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class IssueStatusChangedEvent extends ApplicationEvent {
    private final long issueId;
    private final long fromKanbanConfigId;
    private final long toKanbanConfigId;
    private final Long updaterId;
    private final Long projectId;

    public IssueStatusChangedEvent(Object source, Long issueId, long fromKanbanConfigId, long toKanbanConfig, Long updaterId, Long projectId) {
        super(source);
        this.issueId = issueId;
        this.fromKanbanConfigId = fromKanbanConfigId;
		this.toKanbanConfigId = toKanbanConfig;
        this.updaterId = updaterId;
        this.projectId = projectId;
    }
}
