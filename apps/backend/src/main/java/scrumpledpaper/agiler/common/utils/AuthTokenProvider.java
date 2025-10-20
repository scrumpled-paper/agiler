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
public class AuthTokenProvider {
	private final AppProperties appProperties;
	private final SecretKey key;
	private final SecretKey refreshKey;

	public AuthTokenProvider(AppProperties appProperties) {
		this.appProperties = appProperties;
		this.key = Keys.hmacShaKeyFor(appProperties.getAuth().getTokenSecret().getBytes(StandardCharsets.UTF_8));
		this.refreshKey = Keys.hmacShaKeyFor(appProperties.getAuth().getRefreshTokenSecret().getBytes(StandardCharsets.UTF_8));

	}

	public String createToken(Long userId) {
		return buildToken(userId, key, appProperties.getAuth().getTokenExpiry());
	}

	public String refreshToken(Long userId) {
		return buildToken(userId, refreshKey, appProperties.getAuth().getRefreshTokenExpiry());
	}

	private String buildToken(Long userId, SecretKey signingKey, long expiryTime) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + expiryTime);

		return Jwts.builder()
			.subject(userId.toString())
			.issuedAt(now)
			.expiration(expiryDate)
			.signWith(signingKey)
			.compact();
	}

	public Long getUserIdFromAccessToken(String accessToken) {
		Claims claims = getClaims(accessToken, key);
		return Long.valueOf(claims.getSubject());
	}

	public boolean validateToken(String token) {
		try {
			getClaims(token, key);
			return true;
		} catch (CustomException e) {
			return false;
		}
	}

	private Claims getClaims(String token, SecretKey key) {
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
