package scrumpledpaper.agiler.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateReqDto(
	@NotBlank(message = "닉네임은 비어있을 수 없습니다.")
	@Size(max = 20, message = "닉네임은 최대 20자까지 가능합니다.")
	String nickname,
	String email,
	String description
) {}
