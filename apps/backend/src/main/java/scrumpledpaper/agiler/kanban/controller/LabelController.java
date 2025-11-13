package scrumpledpaper.agiler.kanban.controller;

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
import scrumpledpaper.agiler.kanban.dto.LabelCreateReqDto;
import scrumpledpaper.agiler.kanban.dto.LabelListResDto;
import scrumpledpaper.agiler.kanban.dto.LabelResDto;
import scrumpledpaper.agiler.kanban.dto.LabelUpdateReqDto;
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

	@GetMapping("/{projectUrl}/labels")
	public ResponseEntity<LabelListResDto> getLabels(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl) {
		List<LabelResDto> labels = labelService.getLabelList(customUserDetails.getUserId(), projectUrl);
		LabelListResDto labelListResDto = new LabelListResDto(labels, labels.size());
		return ResponseEntity.ok(labelListResDto);
	}

	@PutMapping("/{projectUrl}/labels/{labelId}")
	public ResponseEntity<Void> updateLabel(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@PathVariable Long labelId,
		@RequestBody @Valid LabelUpdateReqDto labelUpdateReqDto) {
		labelService.updateLabel(customUserDetails.getUserId(), projectUrl, labelId, labelUpdateReqDto);
		return ResponseEntity.noContent().build();
	}
}

