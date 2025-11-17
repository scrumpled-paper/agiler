package scrumpledpaper.agiler.template.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
import scrumpledpaper.agiler.template.dto.ScrumTemplateDetailResDto;
import scrumpledpaper.agiler.template.dto.ScrumTemplateListResDto;
import scrumpledpaper.agiler.template.dto.ScrumTemplateResDto;
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

	@GetMapping("/{projectUrl}/scrums/templates")
	public ResponseEntity<ScrumTemplateListResDto> getScrumTemplates(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl) {
		List<ScrumTemplateResDto> templates = scrumTemplateService.getScrumTemplateList(customUserDetails.getUserId(), projectUrl);
		ScrumTemplateListResDto scrumTemplateListResDto = new ScrumTemplateListResDto(templates, templates.size());
		return ResponseEntity.ok(scrumTemplateListResDto);
	}

	@GetMapping("/{projectUrl}/scrums/templates/{templateId}")
	public ResponseEntity<ScrumTemplateDetailResDto> getScrumTemplate(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@PathVariable Long templateId) {
		ScrumTemplateDetailResDto template = scrumTemplateService.getScrumTemplate(customUserDetails.getUserId(), projectUrl, templateId);
		return ResponseEntity.ok(template);
	}

}
