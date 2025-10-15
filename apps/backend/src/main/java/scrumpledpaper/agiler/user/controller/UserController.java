package scrumpledpaper.agiler.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.resolver.Login;
import scrumpledpaper.agiler.user.dto.TokenResponseDto;
import scrumpledpaper.agiler.user.dto.UserDto;
import scrumpledpaper.agiler.user.dto.UserResDto;
import scrumpledpaper.agiler.user.service.UserService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@PostMapping("/login") // todo
	public ResponseEntity<TokenResponseDto> login(@RequestBody String email) {
		TokenResponseDto tokenResponseDto = userService.login(email);
		return ResponseEntity.created(null).body(tokenResponseDto);
	}


	@GetMapping("/users")
	public ResponseEntity<UserResDto> getUser(@Parameter(hidden = true) @Login UserDto userDto) {
		UserResDto userResDto = userService.getUser(userDto);
		return ResponseEntity.ok(userResDto);
	}
}
