package scrumpledpaper.agiler.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import scrumpledpaper.agiler.common.BaseEntity;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "project")
public class Project extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "url", nullable = false)
	private String url;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "summary")
	private String summary;

	@Column(name = "image_id")
	private Long imageId;

	public void updateDetails(String title, String url, String summary) {
		this.title = title;
		this.url = url;
		this.summary = summary;
	}

	public void updateImageId(Long imageId) {
		this.imageId = imageId;
	}

}
