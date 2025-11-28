package scrumpledpaper.agiler.project.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
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
import scrumpledpaper.agiler.project.dto.ImageUpdateReqDto;
import scrumpledpaper.agiler.project.dto.ProfileResDto;
import scrumpledpaper.agiler.project.dto.ProfileRoleUpdateReqDto;
import scrumpledpaper.agiler.project.dto.ProfileUpdateReqDto;
import scrumpledpaper.agiler.project.service.ProfileService;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProfileController {
	private final ProfileService profileService;

	@GetMapping({"/{projectUrl}/profiles/me"})
	public ResponseEntity<ProfileResDto> getMyProjectProfile(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl) {
		ProfileResDto profileResDto = profileService.getMyProjectProfile(customUserDetails.getUserId(), projectUrl);
		return ResponseEntity.ok(profileResDto);
	}

	@GetMapping("/{projectUrl}/profiles")
	public ResponseEntity<PageResDto<ProfileResDto>> getProjectMembersByUrl(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@ModelAttribute @Valid PageReqDto pageReqDto) {
		Pageable pageable = pageReqDto.toPageable();

		PageResDto<ProfileResDto> pageResDto = profileService.getProjectMembersByUrl(customUserDetails.getUserId(), projectUrl, pageable);
		return ResponseEntity.ok().body(pageResDto);
	}

	@GetMapping({"/{projectUrl}/profiles/{profileId}"})
	public ResponseEntity<ProfileResDto> getProjectProfileById(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@PathVariable Long profileId) {
		ProfileResDto profileResDto = profileService.getProjectProfileById(customUserDetails.getUserId(), projectUrl, profileId);
		return ResponseEntity.ok(profileResDto);
	}

	@PutMapping("/{projectUrl}/profiles")
	public ResponseEntity<Void> updateProfile(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestBody @Valid ProfileUpdateReqDto profileUpdateReqDto) {
		profileService.updateProfile(customUserDetails.getUserId(), projectUrl, profileUpdateReqDto);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{projectUrl}/profiles/role")
	public ResponseEntity<Void> updateProfileRole(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestBody @Valid ProfileRoleUpdateReqDto profileRoleUpdateReqDto) {
		profileService.updateProfileRole(
			customUserDetails.getUserId(),
			projectUrl,
			profileRoleUpdateReqDto.profileId(),
			profileRoleUpdateReqDto.role());
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{projectUrl}/profiles/image")
	public ResponseEntity<Void> updateProfileImage(
			@Parameter(hidden = true)
			@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@PathVariable String projectUrl,
			@RequestBody @Valid ImageUpdateReqDto imageUpdateReqDto
	) {
		profileService.updateProfileImage(customUserDetails.getUserId(), projectUrl, imageUpdateReqDto.objectKey());
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{projectUrl}/profiles/image")
	public ResponseEntity<Void> deleteProfileImage(
			@Parameter(hidden = true)
			@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@PathVariable String projectUrl
	) {
		profileService.deleteProfileImage(customUserDetails.getUserId(), projectUrl);
		return ResponseEntity.noContent().build();
	}

}
