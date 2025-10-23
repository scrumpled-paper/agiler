package scrumpledpaper.agiler.image.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.image.dto.ImageUploadConfirmationRequestDto;
import scrumpledpaper.agiler.image.dto.ImageUploadConfirmationResponseDto;
import scrumpledpaper.agiler.image.dto.PreSignedUrlResponseDto;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.image.repository.ImageRepository;
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

	@Transactional(readOnly = true)
	public PreSignedUrlResponseDto generatePreSignedUrl(User user, String fileName) {
		String objectKey = generateKeyPath(user.getId(), fileName);
		Date expiration = getExpirationTime();

		GeneratePresignedUrlRequest presignedUrlRequest = new GeneratePresignedUrlRequest(bucket, objectKey)
				.withMethod(HttpMethod.PUT)
				.withExpiration(expiration);

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
	public ImageUploadConfirmationResponseDto confirmUpload(ImageUploadConfirmationRequestDto request) {
		String objectKey = request.objectKey();
		String imageUrl = amazonS3.getUrl(bucket, objectKey).toString();

		Image image = Image.builder()
				.url(imageUrl)
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

}
