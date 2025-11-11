package scrumpledpaper.agiler.project.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfileRoleUpdateReqDto(
	long profileId,
	@NotBlank(message = "역할은 비어있을 수 없습니다.")
	String role
) {}
