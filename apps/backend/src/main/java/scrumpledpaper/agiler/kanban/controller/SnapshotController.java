package scrumpledpaper.agiler.kanban.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.auth.service.CustomUserDetails;
import scrumpledpaper.agiler.common.utils.CookieUtils;
import scrumpledpaper.agiler.kanban.service.SnapshotService;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class SnapshotController {
	private final SnapshotService snapshotService;

	@PostMapping("/{projectUrl}/snapshots/today")
	public ResponseEntity<Void> createSnapshotForToday(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		HttpServletResponse response) {
		long time = snapshotService.issueSnapshotAndResetForToday(customUserDetails.getUserId(), projectUrl);
		CookieUtils.addProjectUrlCookie(response, projectUrl, time);
		return ResponseEntity.ok().build();
	}
}
