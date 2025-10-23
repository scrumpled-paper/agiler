package scrumpledpaper.agiler.image.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.image.dto.ImageUploadConfirmationResponseDto;
import scrumpledpaper.agiler.image.dto.PreSignedUrlResponseDto;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.image.enums.ImageContentType;
import scrumpledpaper.agiler.image.repository.ImageRepository;
import scrumpledpaper.agiler.image.event.ImageDeletedEvent;
import scrumpledpaper.agiler.user.entity.User;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;
	private final AmazonS3 amazonS3;
	private final ImageRepository imageRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional(readOnly = true)
	public PreSignedUrlResponseDto generatePreSignedUrl(User user, String fileName, String contentType) {
		String objectKey = generateKeyPath(user.getId(), fileName);
		Date expiration = getExpirationTime();

		ImageContentType imageContentType = ImageContentType.from(contentType);

		GeneratePresignedUrlRequest presignedUrlRequest = new GeneratePresignedUrlRequest(bucket, objectKey)
				.withMethod(HttpMethod.PUT)
				.withExpiration(expiration)
				.withContentType(imageContentType.getMimeType());

		String preSignedUrl = amazonS3.generatePresignedUrl(presignedUrlRequest).toString();

		return new PreSignedUrlResponseDto(preSignedUrl, objectKey);
	}

	private String generateKeyPath(long userId, String fileName) {
		String uuid = UUID.randomUUID().toString();

		return String.format("images/%d/%s-%s", userId, uuid, fileName);
	}

	private Date getExpirationTime() {
		long expTimeMillis = System.currentTimeMillis();
		expTimeMillis += 1000 * 60 * 5; // 5 minutes

		return new Date(expTimeMillis);
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

	@Transactional(readOnly = true)
	public Long findById(Long imageId) {
		Image image = imageRepository.findById(imageId).orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));
		return image.getId();
	}

	@Transactional(readOnly = true)
	public String getImageUrl(Long imageId) {
		Image image = imageRepository.findById(imageId).orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));
		return image.getUrl();
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
