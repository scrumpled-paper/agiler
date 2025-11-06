package scrumpledpaper.agiler.project.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.PageReqDto;
import scrumpledpaper.agiler.common.PageResDto;
import scrumpledpaper.agiler.common.resolver.Login;
import scrumpledpaper.agiler.project.dto.ProjectCheckReqDto;
import scrumpledpaper.agiler.project.dto.ProjectCheckResDto;
import scrumpledpaper.agiler.project.dto.ProjectCreateReqDto;
import scrumpledpaper.agiler.project.dto.ProjectDetailResDto;
import scrumpledpaper.agiler.project.dto.ProjectIdResDto;
import scrumpledpaper.agiler.project.dto.ProjectInfoResDto;
import scrumpledpaper.agiler.project.dto.ProjectSideResDto;
import scrumpledpaper.agiler.project.dto.ProjectUpdateReqDto;
import scrumpledpaper.agiler.project.service.ProjectService;
import scrumpledpaper.agiler.user.dto.UserDto;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {
	private final ProjectService projectService;

	@PostMapping
	public ResponseEntity<ProjectIdResDto> createProject(@Parameter(hidden = true) @Login UserDto userDto, @RequestBody @Valid ProjectCreateReqDto projectCreateReqDto) {
		ProjectIdResDto projectIdResDto = projectService.createProject(userDto, projectCreateReqDto);
		return ResponseEntity.created(null).body(projectIdResDto);
	}

	@GetMapping("/check")
	public ResponseEntity<ProjectCheckResDto> checkProjectUrl(@ModelAttribute @Valid ProjectCheckReqDto projectCheckReqDto) {
		ProjectCheckResDto projectCheckResDto = projectService.checkProjectUrl(projectCheckReqDto);
		return ResponseEntity.ok().body(projectCheckResDto);
	}

	@GetMapping("/info")
	public ResponseEntity<PageResDto<ProjectInfoResDto>> getProjectInfo(@Parameter(hidden = true) @Login UserDto userDto,
		@ModelAttribute @Valid PageReqDto pageReqDto) {
		Pageable pageable = pageReqDto.toPageable();

		PageResDto<ProjectInfoResDto> pageResDto = projectService.getProjectInfo(userDto, pageable);
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
