package scrumpledpaper.agiler.common.utils;

import static scrumpledpaper.agiler.common.exception.ErrorCode.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import scrumpledpaper.agiler.common.config.AppProperties;
import scrumpledpaper.agiler.common.exception.CustomException;

@Component
public class WssTokenProvider {
	private final AppProperties appProperties;
	private final SecretKey key;

	public WssTokenProvider(AppProperties appProperties) {
		this.appProperties = appProperties;
		this.key = Keys.hmacShaKeyFor(appProperties.getApi().getKey().getBytes(StandardCharsets.UTF_8));
	}

	public String createWssToken(Long userId, String docId) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + appProperties.getApi().getWssTokenExpiry());

		return Jwts.builder()
			.subject(userId.toString())
			.claim("docId", docId)
			.issuedAt(now)
			.expiration(expiryDate)
			.signWith(Keys.hmacShaKeyFor(appProperties.getApi().getKey().getBytes(StandardCharsets.UTF_8)))
			.compact();
	}

	public Claims getClaims(String token) {
		try {
			return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
			throw new CustomException(INVALID_TOKEN);
		} catch (ExpiredJwtException e) {
			throw new CustomException(EXPIRED_TOKEN);
		}
	}
}

