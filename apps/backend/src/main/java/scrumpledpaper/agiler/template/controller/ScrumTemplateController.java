package scrumpledpaper.agiler.template.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.auth.service.CustomUserDetails;
import scrumpledpaper.agiler.template.dto.ScrumTemplateCreateReqDto;
import scrumpledpaper.agiler.template.dto.ScrumTemplateUpdateReqDto;
import scrumpledpaper.agiler.template.service.ScrumTemplateService;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ScrumTemplateController {
	private final ScrumTemplateService scrumTemplateService;

	@PostMapping("/{projectUrl}/scrums/templates")
	public ResponseEntity<Void> createScrumTemplate(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestBody ScrumTemplateCreateReqDto scrumTemplateCreateReqDto) {
		@RequestBody @Valid ScrumTemplateCreateReqDto scrumTemplateCreateReqDto) {
		scrumTemplateService.createScrumTemplate(customUserDetails.getUserId(), projectUrl, scrumTemplateCreateReqDto);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{projectUrl}/scrums/templates")
	public ResponseEntity<Void> updateScrumTemplate(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestBody @Valid ScrumTemplateUpdateReqDto scrumTemplateUpdateReqDto) {
		scrumTemplateService.updateScrumTemplate(customUserDetails.getUserId(), projectUrl, scrumTemplateUpdateReqDto);
		return ResponseEntity.noContent().build();
	}

}
