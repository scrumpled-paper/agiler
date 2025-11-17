package scrumpledpaper.agiler.template.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.auth.service.CustomUserDetails;
import scrumpledpaper.agiler.template.dto.RetroTemplateCreateReqDto;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class RetroTemplateController {
	private final RetroTemplateService retroTemplateService;

	@PostMapping("/{projectUrl}/retros/templates")
	public ResponseEntity<Void> createRetroTemplate(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestBody @Valid RetroTemplateCreateReqDto retroTemplateCreateReqDto) {
		retroTemplateService.createRetroTemplate(customUserDetails.getUserId(), projectUrl, retroTemplateCreateReqDto);
		return ResponseEntity.noContent().build();
	}

}

