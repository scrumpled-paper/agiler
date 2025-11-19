package scrumpledpaper.agiler.notification.domain;

import jakarta.persistence.*;
import lombok.*;
import scrumpledpaper.agiler.common.BaseEntity;
import scrumpledpaper.agiler.kanban.entity.Issue;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScheduledNotification extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private long userId;

	@Column(name = "profile_id", nullable = false)
	private long profileId;

	@Column(name = "issue_id", nullable = false)
	private long issueId;

	@Column(nullable = false)
	private LocalDateTime notificationTime;

	@Column(nullable = false)
	private String message;

	@Builder.Default
	private boolean isSent = false;

	public void markAsSent() {
		this.isSent = true;
	}

}
