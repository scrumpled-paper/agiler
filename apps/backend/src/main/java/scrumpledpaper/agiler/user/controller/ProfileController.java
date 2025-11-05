package scrumpledpaper.agiler.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.resolver.Login;
import scrumpledpaper.agiler.project.dto.ProfileResDto;
import scrumpledpaper.agiler.user.dto.UserDto;
import scrumpledpaper.agiler.user.service.ProfileService;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {
	private final ProfileService profileService;

	@GetMapping({"/{projectUrl}"})
	public ResponseEntity<ProfileResDto> getProjectProfile(@Parameter(hidden = true) @Login UserDto userDto,
		@PathVariable String projectUrl) {
		ProfileResDto profileResDto = profileService.getProjectProfile(userDto.getId(), projectUrl);
		return ResponseEntity.ok(profileResDto);
	}
}
