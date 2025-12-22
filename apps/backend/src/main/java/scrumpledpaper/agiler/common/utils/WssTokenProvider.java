package scrumpledpaper.agiler.common.utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import scrumpledpaper.agiler.common.config.AppProperties;

@Component
public class WssTokenProvider {
	private final AppProperties appProperties;

	public WssTokenProvider(AppProperties appProperties) {
		this.appProperties = appProperties;
	}

	public String createWssToken(Long userId, String docId) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + appProperties.getApi().getWssTokenExpiry());

		return Jwts.builder()
			.subject(userId.toString())
			.claim("docId", docId)
			.issuedAt(now)
			.expiration(expiryDate)
			.signWith(Keys.hmacShaKeyFor(appProperties.getAuth().getTokenSecret().getBytes(StandardCharsets.UTF_8)))
			.compact();
	}
}

