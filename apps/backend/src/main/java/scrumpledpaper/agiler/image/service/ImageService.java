package scrumpledpaper.agiler.image.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.image.repository.ImageRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {
	private final ImageRepository imageRepository;

	public Long findById(Long imageId) {
		Image image = imageRepository.findById(imageId).orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));
		return image.getId();
	}
}
