package scrumpledpaper.agiler.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "profile")
public class Profile extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@Column(name = "nickname")
	private String nickname;

	@Column(name = "role", nullable = false)
	@Enumerated(EnumType.STRING)
	private Role role;

	@Column(name = "img_id", nullable = false)
	private long imgId;

	@Column(name = "email")
	private String email;

	@Column(name = "description")
	private String description;
}
