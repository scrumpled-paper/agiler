package scrumpledpaper.agiler.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이미지 업로드 확인 요청 DTO")
public record ImageUploadConfirmationRequestDto(
		@Schema(description = "S3에 업로드된 객체의 키 (Pre-signed URL 생성 시 반환된 값)", example = "images/1/uuid-my-profile.png")
		@NotBlank
		String objectKey
) {
}
