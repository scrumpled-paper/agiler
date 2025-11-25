package scrumpledpaper.agiler.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import scrumpledpaper.agiler.image.dto.PreSignedUrlResponseDto;
import scrumpledpaper.agiler.image.event.ImageDeletedEvent;
import scrumpledpaper.agiler.image.util.S3PreSignedUrlBuilder;

@Component
@RequiredArgsConstructor
public class S3Manager {

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;
	private final AmazonS3 amazonS3;
	private final ApplicationEventPublisher eventPublisher;

	public PreSignedUrlResponseDto generatePreSignedUrl(long userId, String fileName, String contentType) {
		String objectKey = S3PreSignedUrlBuilder.generateObjectKey(userId, fileName);
		GeneratePresignedUrlRequest presignedUrlRequest = S3PreSignedUrlBuilder.build(objectKey, contentType, bucket);
		String preSignedUrl = amazonS3.generatePresignedUrl(presignedUrlRequest).toString();

		return new PreSignedUrlResponseDto(preSignedUrl, objectKey);
	}

	public String getImageUrl(String objectKey) {
		return amazonS3.getUrl(bucket, objectKey).toString();
	}

	public void deleteS3Object(String objectKey) {
		eventPublisher.publishEvent(new ImageDeletedEvent(objectKey));
	}

}
