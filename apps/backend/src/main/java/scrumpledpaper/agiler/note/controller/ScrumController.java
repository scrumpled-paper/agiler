package scrumpledpaper.agiler.note.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.auth.service.CustomUserDetails;
import scrumpledpaper.agiler.common.PageReqDto;
import scrumpledpaper.agiler.common.PageResDto;
import scrumpledpaper.agiler.note.dto.ScrumResDto;
import scrumpledpaper.agiler.note.service.ScrumService;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ScrumController {
	private final ScrumService scrumService;

	@GetMapping("/{projectUrl}/scrums")
	public ResponseEntity<PageResDto<ScrumResDto>> getRetrospects(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@ModelAttribute @Valid PageReqDto pageReqDto) {
		Pageable pageable = pageReqDto.toPageable();
		PageResDto<ScrumResDto> pageResDto = scrumService.getRetrospects(customUserDetails.getUserId(), projectUrl, pageable);
		return ResponseEntity.ok().body(pageResDto);
	}

}
