package scrumpledpaper.agiler.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.notification.dto.OAuthStatePayload;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthStateService {

	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;

	private static final String KEY_PREFIX = "oauth:state:";
	private static final Duration STATE_TTL = Duration.ofMinutes(10);

	public String createState(long userId, long profileId) {
		String state = UUID.randomUUID().toString();
		OAuthStatePayload payload = new OAuthStatePayload(userId, profileId);

		try {
			String json = objectMapper.writeValueAsString(payload);
			String key = KEY_PREFIX + state;
			redisTemplate.opsForValue().set(key, json, STATE_TTL);
			return state;
		} catch (JsonProcessingException e) {
			throw new CustomException(ErrorCode.JSON_PROCESSING_ERROR);
		}
	}

	public OAuthStatePayload consumeState(String state) {
		String key = KEY_PREFIX + state;
		String json = redisTemplate.opsForValue().get(key);

		if (json == null) {
			throw new CustomException(ErrorCode.REDIS_NOT_FOUND_STATE);
		}

		redisTemplate.delete(key);

		try {
			return objectMapper.readValue(json, OAuthStatePayload.class);
		} catch (JsonProcessingException e) {
			throw new CustomException(ErrorCode.JSON_PROCESSING_ERROR);
		}
	}

}
