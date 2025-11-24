package scrumpledpaper.agiler.common.config;

import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class OpenFeignConfig {

	/**
	 * Feign 로그 레벨 설정
	 * - NONE: 로그 없음
	 * - BASIC: 메서드/URL/상태코드/실행시간
	 * - HEADERS: BASIC + 요청/응답 헤더
	 * - FULL: BODY까지 전부
	 */
	@Bean
	public Logger.Level feignLoggerLevel() {
		return Logger.Level.BASIC;
	}

	/**
	 * 타임아웃 설정
	 * - connectTimeoutMillis: 연결 타임아웃
	 * - readTimeoutMillis: 응답 read 타임아웃
	 */
	@Bean
	public Request.Options feignRequestOptions() {
		int connectTimeoutMillis = (int) TimeUnit.SECONDS.toMillis(3);
		int readTimeoutMillis = (int) TimeUnit.SECONDS.toMillis(10);
		return new Request.Options(connectTimeoutMillis, readTimeoutMillis, false);
	}

}
