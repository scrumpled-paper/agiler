package scrumpledpaper.agiler.kanban.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.common.BaseEntity;

@Getter
@Entity
@Table(name = "kanban_config_snapshot")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KanbanConfigSnapshot extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "kanban_config_id")
	private KanbanConfig kanbanConfig;

	@ManyToOne(optional = false)
	@JoinColumn(name = "project_id")
	private Project project;

	@Column(name = "status_name", nullable = false)
	private String statusName;

	@Column(name = "priority", nullable = false)
	private int priority;

	@Column(name = "default_status", nullable = false)
	private boolean defaultStatus;

	@Column(name = "backlog", nullable = false)
	private boolean backlog;

	@Column(name = "is_done", nullable = false)
	private boolean isDone;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
}
