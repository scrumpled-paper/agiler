package scrumpledpaper.agiler.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class IssueStatusChangedEvent extends ApplicationEvent {
    private final Long issueId;
    private final String oldStatus;
    private final String newStatus;
    private final Long updaterId;
    private final Long projectId;

    public IssueStatusChangedEvent(Object source, Long issueId, String oldStatus, String newStatus, Long updaterId, Long projectId) {
        super(source);
        this.issueId = issueId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.updaterId = updaterId;
        this.projectId = projectId;
    }
}
