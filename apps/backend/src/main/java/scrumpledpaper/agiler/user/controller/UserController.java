package scrumpledpaper.agiler.user.controller;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import scrumpledpaper.agiler.auth.service.CustomUserDetails;
import scrumpledpaper.agiler.user.dto.UserResDto;
import scrumpledpaper.agiler.user.dto.UserUpdateReqDto;
import scrumpledpaper.agiler.user.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@GetMapping
	public ResponseEntity<UserResDto> getUser(
			@Parameter(hidden = true)
			@AuthenticationPrincipal CustomUserDetails customUserDetails
	) {
		UserResDto userResDto = userService.getUser(customUserDetails.getUserId());
		return ResponseEntity.ok(userResDto);
	}

	@PatchMapping
	public ResponseEntity<Void> updateUser(
			@Parameter(hidden = true)
			@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@RequestBody @Valid UserUpdateReqDto userUpdateReqDto) {
		userService.updateUser(customUserDetails.getUserId(), userUpdateReqDto);
		return ResponseEntity.noContent().build();
	}

}
