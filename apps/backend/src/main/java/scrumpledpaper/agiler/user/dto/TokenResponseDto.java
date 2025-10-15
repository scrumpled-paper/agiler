package scrumpledpaper.agiler.user.dto;

public record TokenResponseDto(String accessToken, String refreshToken, String tokenType) {
}
