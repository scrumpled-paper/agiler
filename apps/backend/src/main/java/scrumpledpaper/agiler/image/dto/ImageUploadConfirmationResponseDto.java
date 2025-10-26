package scrumpledpaper.agiler.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이미지 업로드 확인 응답 DTO")
public record ImageUploadConfirmationResponseDto(
		@Schema(description = "시스템에 등록된 이미지의 ID", example = "1")
		Long imageId,

		@Schema(description = "업로드된 이미지의 전체 URL", example = "https://s3.ap-northeast-2.amazonaws.com/agiler-bucket/images/1/uuid-my-profile.png")
		String imageUrl
) {
}
