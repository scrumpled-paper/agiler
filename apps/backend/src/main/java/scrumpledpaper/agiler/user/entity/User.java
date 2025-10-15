package scrumpledpaper.agiler.user.entity;

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
@Table(name = "user")
public class User extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "vendor")
	private String vendor;

	@Column(name = "vendor_id")
	private String vendorId;

	@Column(name = "email")
	private String email;

	@Column(name = "nickname", nullable = false)
	private String nickname;

	@Column(name = "image_id", nullable = false)
	private long imageId;

	public void update(String nickname) {
		this.nickname = nickname;
	}
}
