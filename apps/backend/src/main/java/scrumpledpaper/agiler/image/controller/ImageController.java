package scrumpledpaper.agiler.image.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scrumpledpaper.agiler.common.resolver.Login;
import scrumpledpaper.agiler.image.dto.PreSignedUrlRequestDto;
import scrumpledpaper.agiler.image.dto.PreSignedUrlResponseDto;
import scrumpledpaper.agiler.image.service.ImageService;
import scrumpledpaper.agiler.user.entity.User;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

	private final ImageService imageService;

	@PostMapping("/pre-signed-url")
	public ResponseEntity<PreSignedUrlResponseDto> generatePreSignedUrl(
			@Login User user,
			@RequestBody @Valid PreSignedUrlRequestDto request
	) {
		PreSignedUrlResponseDto response = imageService.generatePreSignedUrl(user, request.fileName());
		return ResponseEntity.ok(response);
	}

}
