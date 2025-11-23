package scrumpledpaper.agiler.kanban.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.auth.service.CustomUserDetails;
import scrumpledpaper.agiler.kanban.dto.IssueAssigneesReqDto;
import scrumpledpaper.agiler.kanban.dto.IssueCreateReqDto;
import scrumpledpaper.agiler.kanban.dto.IssueDeleteReqDto;
import scrumpledpaper.agiler.kanban.dto.IssueIdResDto;
import scrumpledpaper.agiler.kanban.dto.IssueUpdateReqDto;
import scrumpledpaper.agiler.kanban.service.IssueService;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class IssueController {
	private final IssueService issueService;

	@PostMapping("/{projectUrl}/issues")
	public ResponseEntity<IssueIdResDto> createIssue(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestBody @Valid IssueCreateReqDto issueCreateReqDto) {
		long issueId = issueService.createIssue(customUserDetails.getUserId(), projectUrl, issueCreateReqDto);
		return ResponseEntity.created(null).body(new IssueIdResDto(issueId));
	}

	@PatchMapping("/{projectUrl}/issues")
	public ResponseEntity<IssueIdResDto> updateIssue(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestBody @Valid IssueUpdateReqDto issueUpdateReqDto) {
		long issueId = issueService.updateIssue(customUserDetails.getUserId(), projectUrl, issueUpdateReqDto);
		return ResponseEntity.ok(new IssueIdResDto(issueId));
	}

	@DeleteMapping("/{projectUrl}/issues")
	public ResponseEntity<Void> deleteIssue(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestBody @Valid IssueDeleteReqDto issueIdResDto) {
		issueService.deleteIssue(customUserDetails.getUserId(), projectUrl, issueIdResDto.issueId());
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{projectUrl}/issues/{issueId}/assignees")
	public ResponseEntity<Void> updateIssueAssignees(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@PathVariable Long issueId,
		@RequestBody @Valid IssueAssigneesReqDto issueAssigneesReqDto) {
		issueService.updateIssueAssignees(customUserDetails.getUserId(), projectUrl, issueId, issueAssigneesReqDto);
		return ResponseEntity.noContent().build();
	}
}
