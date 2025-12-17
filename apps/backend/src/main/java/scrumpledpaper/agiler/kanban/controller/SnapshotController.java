package scrumpledpaper.agiler.kanban.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.auth.service.CustomUserDetails;
import scrumpledpaper.agiler.common.utils.CookieUtils;
import scrumpledpaper.agiler.kanban.dto.SnapshotAvailableResDto;
import scrumpledpaper.agiler.kanban.service.IssueService;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class SnapshotController {
	private final IssueService issueService;

	@PostMapping("/{projectUrl}/snapshots")
	public ResponseEntity<Void> createSnapshotForToday(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		HttpServletResponse response) {
		long time = issueService.issueSnapshotAndResetForToday(customUserDetails.getUserId(), projectUrl);
		CookieUtils.addProjectUrlCookie(response, projectUrl, time);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{projectUrl}/snapshots")
	public ResponseEntity<SnapshotAvailableResDto> getAvailableSnapshotDates(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestParam @NotNull int year,
		@RequestParam @NotNull @Min(1) @Max(12) int month) {
		SnapshotAvailableResDto resDto = issueService.getAvailableSnapshotDates(customUserDetails.getUserId(), projectUrl, year, month);
		return ResponseEntity.ok(resDto);
	}
}
