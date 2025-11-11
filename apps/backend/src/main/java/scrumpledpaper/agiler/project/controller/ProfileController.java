package scrumpledpaper.agiler.project.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.auth.service.CustomUserDetails;
import scrumpledpaper.agiler.common.PageReqDto;
import scrumpledpaper.agiler.common.PageResDto;
import scrumpledpaper.agiler.project.dto.ProfileResDto;
import scrumpledpaper.agiler.project.dto.ProfileUpdateReqDto;
import scrumpledpaper.agiler.project.service.ProjectService;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProfileController {
	private final ProjectService projectService;

	@GetMapping({"/{projectUrl}/profiles/me"})
	public ResponseEntity<ProfileResDto> getMyProjectProfile(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl) {
		ProfileResDto profileResDto = projectService.getMyProjectProfile(customUserDetails.getUserId(), projectUrl);
		return ResponseEntity.ok(profileResDto);
	}

	@GetMapping("/{projectUrl}/profiles")
	public ResponseEntity<PageResDto<ProfileResDto>> getProjectMembersByUrl(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@ModelAttribute @Valid PageReqDto pageReqDto) {
		Pageable pageable = pageReqDto.toPageable();

		PageResDto<ProfileResDto> pageResDto = projectService.getProjectMembersByUrl(customUserDetails.getUserId(), projectUrl, pageable);
		return ResponseEntity.ok().body(pageResDto);
	}

	@GetMapping({"/{projectUrl}/profiles/{profileId}"})
	public ResponseEntity<ProfileResDto> getProjectProfileById(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@PathVariable Long profileId) {
		ProfileResDto profileResDto = projectService.getProjectProfileById(customUserDetails.getUserId(), projectUrl, profileId);
		return ResponseEntity.ok(profileResDto);
	}

	@PutMapping("/{projectUrl}/profiles")
	public ResponseEntity<Void> updateProfile(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestBody @Valid ProfileUpdateReqDto profileUpdateReqDto) {
		projectService.updateProfile(customUserDetails.getUserId(), projectUrl, profileUpdateReqDto);
		return ResponseEntity.noContent().build();
	}
}
