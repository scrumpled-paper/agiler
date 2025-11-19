package scrumpledpaper.agiler.notification.domain;

import jakarta.persistence.*;
import lombok.*;
import scrumpledpaper.agiler.common.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationSubscription extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private long userId;

	@Column(name = "profile_id", nullable = false)
	private long profileId;

	@Column(name = "issue_id")
	private long issueId;

	@Column(name = "from_kanban_config")
	private long fromKanbanConfigId;

	@Column(name = "to_kanban_config")
	private long toKanbanConfigId;

}
