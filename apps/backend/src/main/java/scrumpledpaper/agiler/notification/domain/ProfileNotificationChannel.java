package scrumpledpaper.agiler.notification.domain;

import jakarta.persistence.*;
import lombok.*;
import scrumpledpaper.agiler.common.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileNotificationChannel extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private long userId;

	@Column(name = "profile_id", nullable = false)
	private long profileId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ChannelType channelType;

	@Column(nullable = false)
	private String webhookUrl;

	@Column(nullable = false)
	private String name;

	public void updateWebhookUrl(String webhookUrl) {
		this.webhookUrl = webhookUrl;
	}

}
