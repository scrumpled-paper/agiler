package scrumpledpaper.agiler.template.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import scrumpledpaper.agiler.template.dto.IssueTemplateCreateReqDto;
import scrumpledpaper.agiler.template.dto.IssueTemplateDeleteReqDto;
import scrumpledpaper.agiler.template.dto.IssueTemplateDetailResDto;
import scrumpledpaper.agiler.template.dto.IssueTemplateListResDto;
import scrumpledpaper.agiler.template.dto.IssueTemplateResDto;
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

	@GetMapping("/{projectUrl}/issues/templates")
	public ResponseEntity<IssueTemplateListResDto> getTemplates(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl) {
		List<IssueTemplateResDto> templates = issueTemplateService.getIssueTemplateList(customUserDetails.getUserId(), projectUrl);
		IssueTemplateListResDto labelListResDto = new IssueTemplateListResDto(templates, templates.size());
		return ResponseEntity.ok(labelListResDto);
	}

	@GetMapping("/{projectUrl}/issues/templates/{templateId}")
	public ResponseEntity<IssueTemplateDetailResDto> getTemplate(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@PathVariable Long templateId) {
		IssueTemplateDetailResDto template = issueTemplateService.getIssueTemplate(customUserDetails.getUserId(), projectUrl, templateId);
		return ResponseEntity.ok(template);
	}

	@DeleteMapping("/{projectUrl}/issues/templates")
	public ResponseEntity<Void> deleteTemplate(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestBody IssueTemplateDeleteReqDto issueTemplateDeleteReqDto) {
		issueTemplateService.deleteIssueTemplate(customUserDetails.getUserId(), projectUrl, issueTemplateDeleteReqDto.templateId());
		return ResponseEntity.noContent().build();
	}
}
