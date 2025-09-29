package scrumpledpaper.agiler.project.entity;

import java.time.LocalDateTime;

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
import scrumpledpaper.agiler.user.entity.User;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "user_project")
public class UserProject extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(optional = false)
	@JoinColumn(name = "project_id")
	private Project project;
}
