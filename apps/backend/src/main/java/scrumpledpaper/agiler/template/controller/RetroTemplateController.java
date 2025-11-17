package scrumpledpaper.agiler.template.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.auth.service.CustomUserDetails;
import scrumpledpaper.agiler.template.dto.RetroTemplateCreateReqDto;
import scrumpledpaper.agiler.template.dto.RetroTemplateListResDto;
import scrumpledpaper.agiler.template.dto.RetroTemplateResDto;
import scrumpledpaper.agiler.template.dto.RetroTemplateUpdateReqDto;
import scrumpledpaper.agiler.template.service.RetroTemplateService;

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

	@PutMapping("/{projectUrl}/retros/templates")
	public ResponseEntity<Void> updateRetroTemplate(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestBody @Valid RetroTemplateUpdateReqDto retroTemplateUpdateReqDto) {
		retroTemplateService.updateRetroTemplate(customUserDetails.getUserId(), projectUrl, retroTemplateUpdateReqDto);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{projectUrl}/retros/templates")
	public ResponseEntity<RetroTemplateListResDto> getRetroTemplates(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl) {
		List<RetroTemplateResDto> templates = retroTemplateService.getRetroTemplateList(customUserDetails.getUserId(), projectUrl);
		RetroTemplateListResDto retroTemplateListResDto = new RetroTemplateListResDto(templates, templates.size());
		return ResponseEntity.ok(retroTemplateListResDto);
	}
}

