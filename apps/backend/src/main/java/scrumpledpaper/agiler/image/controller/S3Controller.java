package scrumpledpaper.agiler.image.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import scrumpledpaper.agiler.common.resolver.Login;
import scrumpledpaper.agiler.image.dto.ImageUploadConfirmationRequestDto;
import scrumpledpaper.agiler.image.dto.ImageUploadConfirmationResponseDto;
import scrumpledpaper.agiler.image.dto.PreSignedUrlRequestDto;
import scrumpledpaper.agiler.image.dto.PreSignedUrlResponseDto;
import scrumpledpaper.agiler.image.service.S3Service;
import scrumpledpaper.agiler.user.dto.UserDto;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class S3Controller {

	private final S3Service s3Service;

	@PostMapping("/pre-signed-url")
	public ResponseEntity<PreSignedUrlResponseDto> generatePreSignedUrl(
			@Login UserDto userDto,
			@RequestBody @Valid PreSignedUrlRequestDto request
	) {
		PreSignedUrlResponseDto response = s3Service.generatePreSignedUrl(userDto.getId(), request.fileName(), request.contentType());
		return ResponseEntity.ok(response);
	}

	@PostMapping("/confirm-upload")
	public ResponseEntity<ImageUploadConfirmationResponseDto> confirmUpload(
			@RequestBody @Valid ImageUploadConfirmationRequestDto request
	) {
		ImageUploadConfirmationResponseDto response = s3Service.confirmUpload(request.objectKey());
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{imageId}")
	public ResponseEntity<Void> deleteImage(
			@PathVariable Long imageId
	) {
		s3Service.deleteImage(imageId);
		return ResponseEntity.noContent().build();
	}

}
