package scrumpledpaper.agiler.auth.oauth2;

import scrumpledpaper.agiler.auth.oauth2.userinfo.GitHubOAuth2UserInfo;
import scrumpledpaper.agiler.auth.oauth2.userinfo.GoogleOAuth2UserInfo;
import scrumpledpaper.agiler.auth.oauth2.userinfo.OAuth2UserInfo;

import java.util.Map;
import java.util.function.Function;

public enum ProviderType {
	GOOGLE("google", GoogleOAuth2UserInfo::new),
	GITHUB("github", GitHubOAuth2UserInfo::new);

	private final String registrationId;
	private final Function<Map<String, Object>, OAuth2UserInfo> constructor;

	ProviderType(String registrationId, Function<Map<String, Object>, OAuth2UserInfo> constructor) {
		this.registrationId = registrationId;
		this.constructor = constructor;
	}

	public static ProviderType from(String registrationId) {
		for (ProviderType type : ProviderType.values()) {
			if (type.registrationId.equalsIgnoreCase(registrationId)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unsupported provider: " + registrationId);
	}

	public OAuth2UserInfo getOAuth2UserInfo(Map<String, Object> attributes) {
		return constructor.apply(attributes);
	}

}
