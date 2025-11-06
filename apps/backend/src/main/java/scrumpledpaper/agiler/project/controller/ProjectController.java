package scrumpledpaper.agiler.project.controller;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import scrumpledpaper.agiler.auth.service.CustomUserDetails;
import scrumpledpaper.agiler.common.PageReqDto;
import scrumpledpaper.agiler.common.PageResDto;
import scrumpledpaper.agiler.project.dto.*;
import scrumpledpaper.agiler.project.service.ProjectService;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {
	private final ProjectService projectService;

	@PostMapping
	public ResponseEntity<ProjectCreateResDto> createProject(
			@Parameter(hidden = true)
			@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@RequestBody @Valid ProjectCreateReqDto projectCreateReqDto
	) {
		ProjectCreateResDto projectCreateResDto = projectService.createProject(customUserDetails.getUserId(), projectCreateReqDto);
		return ResponseEntity.created(null).body(projectCreateResDto);
	}

	@GetMapping("/check")
	public ResponseEntity<ProjectCheckResDto> checkProjectUrl(
			@ModelAttribute @Valid ProjectCheckReqDto projectCheckReqDto
	) {
		ProjectCheckResDto projectCheckResDto = projectService.checkProjectUrl(projectCheckReqDto);
		return ResponseEntity.ok().body(projectCheckResDto);
	}

	@GetMapping("/info")
	public ResponseEntity<PageResDto<ProjectInfoResDto>> getProjectInfo(
			@Parameter(hidden = true)
			@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@ModelAttribute @Valid PageReqDto pageReqDto) {
		Pageable pageable = pageReqDto.toPageable();

		PageResDto<ProjectInfoResDto> pageResDto = projectService.getProjectInfo(customUserDetails.getUserId(), pageable);
		return ResponseEntity.ok().body(pageResDto);
	}

	@GetMapping
	public ResponseEntity<PageResDto<ProjectSideResDto>> getProjectSide(@Parameter(hidden = true) @Login UserDto userDto,
		@ModelAttribute @Valid PageReqDto pageReqDto) {
		Pageable pageable = pageReqDto.toPageable();

		PageResDto<ProjectSideResDto> pageResDto = projectService.getProjectSide(userDto, pageable);
		return ResponseEntity.ok().body(pageResDto);
	}

	@GetMapping("/{projectUrl}")
	public ResponseEntity<ProjectDetailResDto> getProjectDetailByUrl(@Parameter(hidden = true) @Login UserDto userDto,
		@PathVariable String projectUrl) {
		ProjectDetailResDto projectDetailResDto = projectService.getProjectDetailByUrl(userDto, projectUrl);
		return ResponseEntity.ok().body(projectDetailResDto);
	}

	@PutMapping("/{projectUrl}")
	public ResponseEntity<ProjectIdResDto> updateProjectDetailByUrl(@Parameter(hidden = true) @Login UserDto userDto,
		@PathVariable String projectUrl,
		@RequestBody @Valid ProjectUpdateReqDto projectUpdateReqDto) {
		ProjectIdResDto projectIdResDto = projectService.updateProjectDetailByUrl(userDto, projectUrl, projectUpdateReqDto);
		return ResponseEntity.ok().body(projectIdResDto);
	}
}
