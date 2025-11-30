package scrumpledpaper.agiler.kanban.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.common.BaseEntity;

@Getter
@Entity
@Table(name = "issue_snapshot")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IssueSnapshot extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "project_id")
	private Project project;

	@ManyToOne(optional = false)
	@JoinColumn(name = "kanban_config_id")
	private KanbanConfig kanbanConfig;

	@ManyToOne
	@JoinColumn(name = "profile_id")
	private Profile profile;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "is_done", nullable = false)
	private boolean isDone;

	@Column(name = "contents")
	private String contents;

	@Column(name = "started_at")
	private LocalDateTime startedAt;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
}
