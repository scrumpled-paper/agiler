package scrumpledpaper.agiler.note.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import scrumpledpaper.agiler.note.dto.internal.NoteContentsUpdateReqDto;
import scrumpledpaper.agiler.note.dto.internal.NoteDataResDto;
import scrumpledpaper.agiler.note.dto.internal.NotePermissionResDto;
import scrumpledpaper.agiler.note.service.InternalNoteService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/notes")
public class InternalNoteController {

	private final InternalNoteService internalNoteService;

	@GetMapping("/{type}/{id}")
	public ResponseEntity<NoteDataResDto> getNote(
			@PathVariable String type,
			@PathVariable Long id
	) {
		NoteDataResDto noteData = internalNoteService.getNote(type, id);
		return ResponseEntity.ok(noteData);
	}

	@PutMapping("/{type}/{id}/contents")
	public ResponseEntity<Void> updateNoteContents(
			@PathVariable String type,
			@PathVariable Long id,
			@RequestBody NoteContentsUpdateReqDto request
	) {
		internalNoteService.updateNoteContents(type, id, request.contents());
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/permission")
	public ResponseEntity<NotePermissionResDto> checkPermission(
			@RequestParam String type,
			@RequestParam Long id,
			@RequestParam Long userId
	) {
		NotePermissionResDto permission = internalNoteService.checkPermission(type, id, userId);
		return ResponseEntity.ok(permission);
	}

}
