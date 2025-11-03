package scrumpledpaper.agiler.auth.oauth2;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOAuth2User extends DefaultOAuth2User {
	private final Long userId;

	public CustomOAuth2User(
			Long userId,
			Collection<? extends GrantedAuthority> authorities,
			Map<String, Object> attributes,
			String nameAttributeKey
	) {
		super(authorities, attributes, nameAttributeKey);
		this.userId = userId;
	}

}
