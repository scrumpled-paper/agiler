package scrumpledpaper.agiler.note.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.auth.service.CustomUserDetails;
import scrumpledpaper.agiler.note.dto.RetroDetailResDto;
import scrumpledpaper.agiler.note.dto.WssTokenResDto;
import scrumpledpaper.agiler.note.service.WssService;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class WssController {
	private final WssService wssService;

	@GetMapping("/api/v1/projects/{projectUrl}/{docId}")
	public ResponseEntity<WssTokenResDto> generateWssToken(
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable String projectUrl,
		@PathVariable String docId) {
		String token = wssService.generateWssToken(customUserDetails.getUserId(), projectUrl, docId);
		return ResponseEntity.ok(new WssTokenResDto(token));
	}

	@GetMapping("/internal/api/v1/docs/retro/{id}")
	public ResponseEntity<RetroDetailResDto> getRetroDetail(
		@PathVariable long id) {
		RetroDetailResDto retroDetail = wssService.getRetroDetail(id);
		return ResponseEntity.ok(retroDetail);
	}
}
