package scrumpledpaper.agiler.note.entity;

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
import scrumpledpaper.agiler.common.BaseEntity;
import scrumpledpaper.agiler.project.entity.Profile;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "meeting_profile")
public class MeetingProfile extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "meeting_id")
	private Meeting meeting;

	@ManyToOne(optional = false)
	@JoinColumn(name = "profile_id")
	private Profile profile;
}
