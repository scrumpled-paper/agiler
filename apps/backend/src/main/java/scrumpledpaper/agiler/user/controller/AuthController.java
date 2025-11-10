package scrumpledpaper.agiler.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scrumpledpaper.agiler.common.utils.CookieUtils;
import scrumpledpaper.agiler.user.dto.TokenResponseDto;
import scrumpledpaper.agiler.user.service.UserService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {
	private final UserService userService;

	@PostMapping("/login") // todo
	public ResponseEntity<TokenResponseDto> login(@RequestBody String email) {
		TokenResponseDto tokenResponseDto = userService.login(email);
		return ResponseEntity.created(null).body(tokenResponseDto);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		CookieUtils.deleteCookie(request, response, "accessToken");
		return ResponseEntity.noContent().build();
	}

}
