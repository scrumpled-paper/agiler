package scrumpledpaper.agiler.kanban.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.auth.service.CustomUserDetails;
import scrumpledpaper.agiler.kanban.dto.KanbanConfigListResDto;
import scrumpledpaper.agiler.kanban.dto.KanbanConfigResDto;
import scrumpledpaper.agiler.kanban.dto.KanbanConfigUpdateReqDto;
import scrumpledpaper.agiler.kanban.service.KanbanConfigService;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class KanbanConfigController {
	private final KanbanConfigService kanbanConfigService;

	@PutMapping("/{projectUrl}/kanban-config")
	public ResponseEntity<Void> updateKanbanConfig(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestBody @Valid KanbanConfigUpdateReqDto kanbanConfigUpdateReqDto) {
		kanbanConfigService.updateKanbanConfig(customUserDetails.getUserId(), projectUrl, kanbanConfigUpdateReqDto);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{projectUrl}/kanban-config")
	public ResponseEntity<KanbanConfigListResDto> getKanbanConfigList(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl) {
		List<KanbanConfigResDto> dtos = kanbanConfigService.getKanbanConfigList(customUserDetails.getUserId(), projectUrl);
		return ResponseEntity.ok(new KanbanConfigListResDto(dtos, dtos.size()));
	}
}
