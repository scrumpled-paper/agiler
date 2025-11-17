package scrumpledpaper.agiler.template.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.auth.service.CustomUserDetails;
import scrumpledpaper.agiler.template.dto.MeetingTemplateCreateReqDto;
import scrumpledpaper.agiler.template.dto.MeetingTemplateUpdateReqDto;
import scrumpledpaper.agiler.template.service.MeetingTemplateService;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class MeetingTemplateController {
	private final MeetingTemplateService meetingTemplateService;

	@PostMapping("/{projectUrl}/meetings/templates")
	public ResponseEntity<Void> createMeetingTemplate(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestBody @Valid MeetingTemplateCreateReqDto meetingTemplateCreateReqDto) {
		meetingTemplateService.createMeetingTemplate(customUserDetails.getUserId(), projectUrl, meetingTemplateCreateReqDto);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{projectUrl}/meetings/templates")
	public ResponseEntity<Void> updateMeetingTemplate(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@RequestBody @Valid MeetingTemplateUpdateReqDto meetingTemplateUpdateReqDto) {
		meetingTemplateService.updateMeetingTemplate(customUserDetails.getUserId(), projectUrl, meetingTemplateUpdateReqDto);
		return ResponseEntity.noContent().build();
	}

}

