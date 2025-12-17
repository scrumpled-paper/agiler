package scrumpledpaper.agiler.kanban.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import scrumpledpaper.agiler.common.BaseEntity;
import scrumpledpaper.agiler.project.entity.Project;

@Getter
@Entity
@Table(name = "issue")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

	@OneToMany(mappedBy = "issue", fetch = FetchType.LAZY)
	@Builder.Default
	private List<IssueProfile> assignees = new ArrayList<>();

	@OneToMany(mappedBy = "issue", fetch = FetchType.LAZY)
	@Builder.Default
	@BatchSize(size = 100)
	private List<IssueLabel> labels = new ArrayList<>();

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

	public void update(String title, String contents) {
		this.title = title;
		this.contents = contents;
	}

	public void updateKanbanConfig(KanbanConfig kanbanConfig) {
		this.kanbanConfig = kanbanConfig;
		this.isDone = kanbanConfig.getIsDone();
	}
}
