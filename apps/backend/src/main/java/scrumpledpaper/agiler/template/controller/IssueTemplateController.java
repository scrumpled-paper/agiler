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
import scrumpledpaper.agiler.template.dto.IssueTemplateCreateReqDto;
import scrumpledpaper.agiler.template.dto.IssueTemplateUpdateReqDto;
import scrumpledpaper.agiler.template.service.IssueTemplateService;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class IssueTemplateController {
	private final IssueTemplateService issueTemplateService;

	@PostMapping("/{projectUrl}/issues/templates")
	public ResponseEntity<Void> createTemplate(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestBody @Valid IssueTemplateCreateReqDto issueTemplateCreateReqDto) {
		issueTemplateService.createIssueTemplate(customUserDetails.getUserId(), projectUrl, issueTemplateCreateReqDto);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{projectUrl}/issues/templates")
	public ResponseEntity<Void> updateTemplate(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestBody @Valid IssueTemplateUpdateReqDto issueTemplateCreateReqDto) {
		issueTemplateService.updateIssueTemplate(customUserDetails.getUserId(), projectUrl, issueTemplateCreateReqDto);
		return ResponseEntity.noContent().build();
	}
}
