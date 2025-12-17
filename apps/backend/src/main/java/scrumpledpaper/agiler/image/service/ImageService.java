package scrumpledpaper.agiler.image.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.image.repository.ImageRepository;

import java.util.List;
import java.util.Map;
import java.util.function.LongConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

	private final S3Manager s3Manager;
	private final ImageRepository imageRepository;

	private static final long DEFAULT_IMAGE_ID = 1L;

	@Transactional(readOnly = true)
	public Image findById(Long imageId) {
		return imageRepository.findById(imageId).orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public String getImageUrlById(Long imageId) {
		Image image = findById(imageId);
		return image.getUrl();
	}

	/**
	* @param getImageId function to get current image ID <br>
	* 						e.g. () -> entity.getImageId()
	* @param updateImageId function to update image ID <br>
	* 						e.g. (newImageId) -> entity.updateImageId(newImageId)
	* @param objectKey S3 object key
	* */
	@Transactional
	public void updateImage(Supplier<Long> getImageId, LongConsumer updateImageId, String objectKey) {
		Long currentImageId = getImageId.get();
		if (currentImageId != null && currentImageId != DEFAULT_IMAGE_ID) {
			deleteById(currentImageId);
		}

		Image image = createImageEntity(objectKey);
		Image savedImage = imageRepository.save(image);
		updateImageId.accept(savedImage.getId());
	}

	/**
	* @param getImageId function to get current image ID <br>
	* 						e.g. () -> entity.getImageId()
	* @param updateImageId function to update image ID <br>
	* 						e.g. (newImageId) -> entity.updateImageId(newImageId)
	* */
	@Transactional
	public void deleteImage(Supplier<Long> getImageId, LongConsumer updateImageId) {
		Long currentImageId = getImageId.get();
		if (currentImageId == null || currentImageId == DEFAULT_IMAGE_ID) {
			return;
		}

		deleteById(currentImageId);
		updateImageId.accept(DEFAULT_IMAGE_ID);
	}


	private void deleteById(long imageId) {
		Image image = findById(imageId);
		s3Manager.deleteS3Object(image.getObjectKey());
		imageRepository.delete(image);
	}

	private Image createImageEntity(String objectKey) {
		String imageUrl = s3Manager.getImageUrl(objectKey);

		return Image.builder()
				.url(imageUrl)
				.objectKey(objectKey)
				.build();
	}

	public Map<Long, String> getImageUrlsByIds(List<Long> imageIds) {
		if (imageIds == null || imageIds.isEmpty()) {
			return Map.of();
		}

		List<Image> images = imageRepository.findAllById(imageIds);

		return images.stream()
			.collect(Collectors.toMap(Image::getId, Image::getUrl));
	}
}
