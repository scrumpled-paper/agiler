package scrumpledpaper.agiler.config;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.*;

@TestConfiguration(proxyBeanMethods = false)
public class S3FallbackConfiguration {

	@Bean
	@Primary
	@ConditionalOnMissingBean(AmazonS3.class) // 이미 다른 AmazonS3가 있으면 생성 안 함
	public AmazonS3 amazonS3Stub() {
		AmazonS3 s3 = mock(AmazonS3.class, withSettings().stubOnly());
		// 최소 기본 동작만 스텁해서 의존 빈들이 죽지 않게
		when(s3.doesBucketExistV2(anyString())).thenReturn(true);
		return s3;
	}

}
