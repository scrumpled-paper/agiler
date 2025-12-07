package scrumpledpaper.agiler.kanban.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;
import scrumpledpaper.agiler.common.BaseEntity;
import scrumpledpaper.agiler.project.entity.Project;

@Getter
@Entity
@Table(name = "issue_snapshot_date_mapping")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IssueSnapshotDateMapping extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "project_id")
	private Project project;

	@Column(name = "issue_count", nullable = false)
	private int issueCount;

	@Column(name = "snapshot_date", nullable = false)
	private LocalDate snapshotDate;

	public void decrementIssueSnapshotMappingCount() {
		this.issueCount -= 1;
	}
}
