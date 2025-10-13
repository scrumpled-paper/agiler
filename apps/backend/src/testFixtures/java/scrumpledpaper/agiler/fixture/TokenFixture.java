package scrumpledpaper.agiler.fixture;

import org.springframework.stereotype.Component;

import scrumpledpaper.agiler.common.utils.AuthTokenProvider;
import scrumpledpaper.agiler.user.entity.User;

@Component
public class TokenFixture {
	private final AuthTokenProvider authTokenProvider;

	public TokenFixture(AuthTokenProvider authTokenProvider) {
		this.authTokenProvider = authTokenProvider;
	}

	public String createAccessToken(User user) {
		return authTokenProvider.createToken(user.getId());
	}
}
