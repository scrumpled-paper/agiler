package scrumpledpaper.agiler.image.dto;

public record PreSignedUrlResponseDto(
		String preSignedUrl,
		String objectKey
) {
}
