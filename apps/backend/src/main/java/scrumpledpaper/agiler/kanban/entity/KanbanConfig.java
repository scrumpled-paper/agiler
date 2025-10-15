package scrumpledpaper.agiler.kanban.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.common.BaseEntity;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "kanban_config")
public class KanbanConfig extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne
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
	private boolean done;
}
