package scrumpledpaper.agiler.image.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import scrumpledpaper.agiler.common.BaseEntity;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "image")
public class Image extends BaseEntity {
	@Id
	@Column(name = "profile_id")
	private Long profileId;

	@Column(name = "url", nullable = false)
	private String url;
}
