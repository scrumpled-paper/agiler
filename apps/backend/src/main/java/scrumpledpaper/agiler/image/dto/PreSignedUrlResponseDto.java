package scrumpledpaper.agiler.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pre-signed URL 생성 응답 DTO")
public record PreSignedUrlResponseDto(
		@Schema(description = "S3에 업로드하기 위한 Pre-signed URL", example = "https://s3.ap-northeast-2.amazonaws.com/agiler-bucket/images/1/uuid-my-profile.png?...")
		String preSignedUrl,

		@Schema(description = "S3에 업로드된 객체의 키 (업로드 확인 시 사용)", example = "images/1/uuid-my-profile.png")
		String objectKey
) {
}
