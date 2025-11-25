package scrumpledpaper.agiler.image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scrumpledpaper.agiler.auth.service.CustomUserDetails;
import scrumpledpaper.agiler.image.dto.PreSignedUrlRequestDto;
import scrumpledpaper.agiler.image.dto.PreSignedUrlResponseDto;
import scrumpledpaper.agiler.image.service.S3Manager;

@Tag(name = "S3", description = "S3 이미지 업로드 관련 API")
@RestController
@RequestMapping("/api/v1/s3")
@RequiredArgsConstructor
public class S3Controller {

	private final S3Manager s3Manager;

	@Operation(summary = "Pre-signed URL 생성", description = "S3에 이미지를 업로드하기 위한 Pre-signed URL을 생성합니다.")
	@PostMapping("/pre-signed-url")
	public ResponseEntity<PreSignedUrlResponseDto> generatePreSignedUrl(
			@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@RequestBody @Valid PreSignedUrlRequestDto request
	) {
		PreSignedUrlResponseDto response = s3Manager.generatePreSignedUrl(customUserDetails.getUserId(), request.fileName(), request.contentType());
		return ResponseEntity.ok(response);
	}

}
