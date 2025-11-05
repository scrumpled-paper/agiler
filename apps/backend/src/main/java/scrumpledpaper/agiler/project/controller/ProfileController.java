package scrumpledpaper.agiler.project.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.PageReqDto;
import scrumpledpaper.agiler.common.PageResDto;
import scrumpledpaper.agiler.common.resolver.Login;
import scrumpledpaper.agiler.project.dto.ProfileResDto;
import scrumpledpaper.agiler.project.service.ProjectService;
import scrumpledpaper.agiler.user.dto.UserDto;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProfileController {
	private final ProjectService projectService;

	@GetMapping({"/{projectUrl}/profiles/me"})
	public ResponseEntity<ProfileResDto> getMyProjectProfile(@Parameter(hidden = true) @Login UserDto userDto,
		@PathVariable String projectUrl) {
		ProfileResDto profileResDto = projectService.getMyProjectProfile(userDto.getId(), projectUrl);
		return ResponseEntity.ok(profileResDto);
	}

	@GetMapping("/{projectUrl}/profiles")
	public ResponseEntity<PageResDto<ProfileResDto>> getProjectMembersByUrl(@Parameter(hidden = true) @Login UserDto userDto,
		@PathVariable String projectUrl,
		@ModelAttribute @Valid PageReqDto pageReqDto) {
		Pageable pageable = pageReqDto.toPageable();

		PageResDto<ProfileResDto> pageResDto = projectService.getProjectMembersByUrl(userDto, projectUrl, pageable);
		return ResponseEntity.ok().body(pageResDto);
	}
}
