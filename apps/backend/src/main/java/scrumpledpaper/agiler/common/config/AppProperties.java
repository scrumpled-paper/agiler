package scrumpledpaper.agiler.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
public class AppProperties {

	private final Auth auth = new Auth();

	@Getter
	@Setter
	public static class Auth {
		private String tokenSecret;
		private String refreshTokenSecret;
		private long tokenExpiry;
		private long refreshTokenExpiry;
	}
}

