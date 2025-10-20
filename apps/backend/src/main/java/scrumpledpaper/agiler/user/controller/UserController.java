package scrumpledpaper.agiler.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.resolver.Login;
import scrumpledpaper.agiler.user.dto.UserDto;
import scrumpledpaper.agiler.user.dto.UserResDto;
import scrumpledpaper.agiler.user.dto.UserUpdateReqDto;
import scrumpledpaper.agiler.user.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@GetMapping
	public ResponseEntity<UserResDto> getUser(@Parameter(hidden = true) @Login UserDto userDto) {
		UserResDto userResDto = userService.getUser(userDto);
		return ResponseEntity.ok(userResDto);
	}

	@PatchMapping
	public ResponseEntity<Void> updateUser(@Parameter(hidden = true) @Login UserDto userDto, @RequestBody @Valid UserUpdateReqDto userUpdateReqDto) {
		userService.updateUser(userDto, userUpdateReqDto);
		return ResponseEntity.noContent().build();
	}
}
