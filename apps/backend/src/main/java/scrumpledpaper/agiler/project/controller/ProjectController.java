package scrumpledpaper.agiler.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.resolver.Login;
import scrumpledpaper.agiler.project.dto.ProjectCreateReqDto;
import scrumpledpaper.agiler.project.dto.ProjectCreateResDto;
import scrumpledpaper.agiler.project.service.ProjectService;
import scrumpledpaper.agiler.user.dto.UserDto;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {
	private final ProjectService projectService;

	@PostMapping
	public ResponseEntity<ProjectCreateResDto> createProject(@Parameter(hidden = true) @Login UserDto userDto, @RequestBody @Valid ProjectCreateReqDto projectCreateReqDto) {
		ProjectCreateResDto projectCreateResDto = projectService.createProject(userDto, projectCreateReqDto);
		return ResponseEntity.created(null).body(projectCreateResDto);
	}
}
