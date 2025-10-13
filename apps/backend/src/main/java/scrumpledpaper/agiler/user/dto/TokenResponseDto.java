package scrumpledpaper.agiler.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponseDto {
	private String accessToken;
	private String refreshToken;
	private String tokenType;
}
