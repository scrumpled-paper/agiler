package scrumpledpaper.agiler.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfileRoleUpdateReqDto(
	@NotNull(message = "프로필 ID는 비어있을 수 없습니다.")
	Long profileId,
	@NotBlank(message = "역할은 비어있을 수 없습니다.")
	String role
) {}
