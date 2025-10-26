package scrumpledpaper.agiler.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Pre-signed URL 생성 요청 DTO")
public record PreSignedUrlRequestDto(
		@Schema(description = "업로드할 파일의 원본 이름", example = "my-profile.png")
		@NotBlank
		String fileName,

		@Schema(description = "업로드할 파일의 Content-Type", example = "image/png")
		@NotBlank
		String contentType
) {
}
