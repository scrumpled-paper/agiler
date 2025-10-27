package scrumpledpaper.agiler.image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "S3", description = "S3 이미지 업로드 관련 API")
@RestController
@RequestMapping("/api/v1/s3")
@RequiredArgsConstructor
public class S3Controller {

	private final S3Service s3Service;

	@Operation(summary = "Pre-signed URL 생성", description = "S3에 이미지를 업로드하기 위한 Pre-signed URL을 생성합니다.")
	@PostMapping("/pre-signed-url")
	public ResponseEntity<PreSignedUrlResponseDto> generatePreSignedUrl(
			@Login UserDto userDto,
			@RequestBody @Valid PreSignedUrlRequestDto request
	) {
		PreSignedUrlResponseDto response = s3Service.generatePreSignedUrl(userDto.getId(), request.fileName(), request.contentType());
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "이미지 저장", description = "Pre-signed URL을 통해 S3에 이미지 업로드가 완료된 후에 이미지 정보를 데이터베이스에 등록합니다.")
	@PostMapping("/confirm-upload")
	public ResponseEntity<ImageUploadConfirmationResponseDto> confirmUpload(
			@RequestBody @Valid ImageUploadConfirmationRequestDto request
	) {
		ImageUploadConfirmationResponseDto response = s3Service.confirmUpload(request.objectKey());
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "이미지 삭제", description = "지정된 ID의 이미지를 S3와 시스템에서 모두 삭제합니다.")
	@DeleteMapping("/{imageId}")
	public ResponseEntity<Void> deleteImage(
			@PathVariable Long imageId
	) {
		s3Service.deleteImage(imageId);
		return ResponseEntity.noContent().build();
	}

}
