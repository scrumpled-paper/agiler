package scrumpledpaper.agiler.kanban.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.auth.service.CustomUserDetails;
import scrumpledpaper.agiler.kanban.dto.IssueCreateReqDto;
import scrumpledpaper.agiler.kanban.service.IssueService;
import scrumpledpaper.agiler.kanban.service.KanbanConfigService;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class IssueController {
	private final IssueService issueService;
	private final KanbanConfigService kanbanConfigService;

	@PostMapping("/{projectUrl}/issues")
	public ResponseEntity<Void> createIssue(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestBody @Valid IssueCreateReqDto issueCreateReqDto) {
		issueService.createIssue(customUserDetails.getUserId(), projectUrl, issueCreateReqDto);
		return ResponseEntity.created(null).build();
	}
}
