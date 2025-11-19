package scrumpledpaper.agiler.kanban.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.common.BaseEntity;
import scrumpledpaper.agiler.project.entity.Project;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "issue")
public class Issue extends BaseEntity {
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
	private Boolean isDone;

	@Column(name = "contents")
	private String contents;

	@Column(name = "started_at")
	private LocalDateTime startedAt;

	@Column(name = "due_at")
	private LocalDateTime dueAt;
}
