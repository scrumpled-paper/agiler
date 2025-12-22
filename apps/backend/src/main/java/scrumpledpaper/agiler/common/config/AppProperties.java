package scrumpledpaper.agiler.common.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

	private final Auth auth = new Auth();
	private final OAuth2 oauth2 = new OAuth2();
	private final Api api = new Api();

	@Getter
	@Setter
	public static class Auth {
		private String tokenSecret;
		private String refreshTokenSecret;
		private long tokenExpiry;
		private long refreshTokenExpiry;
	}

	@Getter
	@Setter
	public static final class OAuth2 {
		private List<String> authorizedRedirectUris = new ArrayList<>();
	}

	@Getter
	@Setter
	public static class Api {
		private String key;
		private long wssTokenExpiry;
	}
}
