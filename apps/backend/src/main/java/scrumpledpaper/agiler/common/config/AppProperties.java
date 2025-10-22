package scrumpledpaper.agiler.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

	private Auth auth = new Auth();

	@Getter
	@Setter
	public static class Auth {
		private String tokenSecret;
		private String refreshTokenSecret;
		private long tokenExpiry;
		private long refreshTokenExpiry;
	}
}
