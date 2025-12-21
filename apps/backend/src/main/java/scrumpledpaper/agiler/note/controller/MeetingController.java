package scrumpledpaper.agiler.note.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.auth.service.CustomUserDetails;
import scrumpledpaper.agiler.common.PageReqDto;
import scrumpledpaper.agiler.common.PageResDto;
import scrumpledpaper.agiler.note.dto.IdResDto;
import scrumpledpaper.agiler.note.dto.MeetingResDto;
import scrumpledpaper.agiler.note.dto.NoteCreateReqDto;
import scrumpledpaper.agiler.note.service.MeetingService;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class MeetingController {
	private final MeetingService meetingService;

	@GetMapping("/{projectUrl}/meetings")
	public ResponseEntity<PageResDto<MeetingResDto>> getMeetings(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@ModelAttribute @Valid PageReqDto pageReqDto) {
		Pageable pageable = pageReqDto.toPageable();
		PageResDto<MeetingResDto> pageResDto = meetingService.getMeetings(customUserDetails.getUserId(), projectUrl, pageable);
		return ResponseEntity.ok().body(pageResDto);
	}

	@PostMapping("/{projectUrl}/meetings")
	public ResponseEntity<IdResDto> createMeeting(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestBody @Valid NoteCreateReqDto noteCreateReqDto) {
		long id = meetingService.createMeeting(customUserDetails.getUserId(), projectUrl, noteCreateReqDto);
		return ResponseEntity.ok().body(new IdResDto(id));
	}
}
