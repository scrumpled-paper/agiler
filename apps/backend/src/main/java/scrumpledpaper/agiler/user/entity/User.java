package scrumpledpaper.agiler.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import scrumpledpaper.agiler.auth.oauth2.ProviderType;
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

	@Enumerated(EnumType.STRING)
	@Column(name = "vendor")
	private ProviderType vendor;

	@Column(name = "vendor_id")
	private String vendorId;

	@Column(name = "email")
	private String email;

	@Column(name = "nickname", nullable = false)
	private String nickname;

	@Column(name = "image_id", nullable = false)
	private long imageId;

	public void update(String email, String nickname){
		this.email = email;
		this.nickname = nickname;
	}
}
