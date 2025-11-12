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
import scrumpledpaper.agiler.kanban.dto.LabelCreateReqDto;
import scrumpledpaper.agiler.kanban.service.LabelService;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class LabelController {
	private final LabelService labelService;

	@PostMapping("/{projectUrl}/labels")
	public ResponseEntity<Void> createLabel(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestBody @Valid LabelCreateReqDto labelCreateReqDto) {
		labelService.createLabel(customUserDetails.getUserId(), projectUrl, labelCreateReqDto);
		return ResponseEntity.created(null).build();
	}
}

