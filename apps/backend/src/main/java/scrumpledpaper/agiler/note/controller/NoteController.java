package scrumpledpaper.agiler.note.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scrumpledpaper.agiler.auth.service.CustomUserDetails;
import scrumpledpaper.agiler.note.dto.NoteCreateReqDto;
import scrumpledpaper.agiler.note.dto.NoteCreateResDto;
import scrumpledpaper.agiler.note.service.NoteService;

@Tag(name = "Note", description = "Note 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectUrl}/notes")
public class NoteController {

	private final NoteService noteService;

	@Operation(summary = "노트 생성", description = "프로젝트에 노트를 생성합니다.")
	@PostMapping("")
	public ResponseEntity<NoteCreateResDto> createNote(
			@Parameter(hidden = true)
			@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@PathVariable String projectUrl,
			@Valid NoteCreateReqDto request
	) {
		NoteCreateResDto createdNote = noteService.createNote(customUserDetails.getUserId(), projectUrl, request.type());
		return ResponseEntity.ok(createdNote);
	}

}
