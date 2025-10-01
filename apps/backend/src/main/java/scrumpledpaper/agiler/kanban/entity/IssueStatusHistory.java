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
import scrumpledpaper.agiler.user.entity.Profile;
import scrumpledpaper.agiler.common.BaseEntity;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "issue_status_history")
public class IssueStatusHistory extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "issue_id")
	private Issue issue;

	@ManyToOne(optional = false)
	@JoinColumn(name = "profile_id")
	private Profile profile;

	@ManyToOne(optional = false)
	@JoinColumn(name = "from_kanban_config")
	private KanbanConfig fromKanbanConfig;

	@ManyToOne(optional = false)
	@JoinColumn(name = "to_kanban_config")
	private KanbanConfig toKanbanConfig;
}
