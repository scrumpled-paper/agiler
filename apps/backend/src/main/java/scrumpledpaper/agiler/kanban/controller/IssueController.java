package scrumpledpaper.agiler.kanban.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import scrumpledpaper.agiler.auth.service.CustomUserDetails;
import scrumpledpaper.agiler.kanban.dto.IssueStatusUpdateReqDto;
import scrumpledpaper.agiler.kanban.service.KanbanService;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class IssueController {

	private final KanbanService kanbanService;

	@PatchMapping("/{projectUrl}/issues/{issueId}/status")
	public ResponseEntity<Void> updateIssueStatus(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable Long issueId,
			@PathVariable String projectUrl,
			@RequestBody @Valid IssueStatusUpdateReqDto request
	) {
		kanbanService.changeIssueStatus(userDetails.getUserId(), issueId, projectUrl, request);
		return ResponseEntity.ok().build();
	}
}
