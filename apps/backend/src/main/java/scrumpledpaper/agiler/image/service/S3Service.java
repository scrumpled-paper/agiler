package scrumpledpaper.agiler.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.image.dto.ImageUploadConfirmationResponseDto;
import scrumpledpaper.agiler.image.dto.PreSignedUrlResponseDto;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.image.event.ImageDeletedEvent;
import scrumpledpaper.agiler.image.repository.ImageRepository;
import scrumpledpaper.agiler.image.util.S3PreSignedUrlBuilder;

@Service
@RequiredArgsConstructor
public class S3Service {

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;
	private final AmazonS3 amazonS3;
	private final ImageRepository imageRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional(readOnly = true)
	public PreSignedUrlResponseDto generatePreSignedUrl(long userId, String fileName, String contentType) {
		String objectKey = S3PreSignedUrlBuilder.generateObjectKey(userId, fileName);
		GeneratePresignedUrlRequest presignedUrlRequest = S3PreSignedUrlBuilder.build(objectKey, contentType, bucket);
		String preSignedUrl = amazonS3.generatePresignedUrl(presignedUrlRequest).toString();

		return new PreSignedUrlResponseDto(preSignedUrl, objectKey);
	}

	@Transactional
	public ImageUploadConfirmationResponseDto confirmUpload(String objectKey) {
		String imageUrl = amazonS3.getUrl(bucket, objectKey).toString();

		Image image = Image.builder()
				.url(imageUrl)
				.objectKey(objectKey)
				.build();

		Image savedImage = imageRepository.save(image);

		return new ImageUploadConfirmationResponseDto(savedImage.getId(), savedImage.getUrl());
	}

	@Transactional
	public void deleteImage(Long imageId) {
		Image image = imageRepository.findById(imageId)
				.orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));
		String objectKey = image.getObjectKey();

		imageRepository.delete(image);

		eventPublisher.publishEvent(new ImageDeletedEvent(objectKey));
	}

}
