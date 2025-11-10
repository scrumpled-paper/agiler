package scrumpledpaper.agiler.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import scrumpledpaper.agiler.auth.oauth2.CustomOAuth2User;
import scrumpledpaper.agiler.auth.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import scrumpledpaper.agiler.common.utils.AuthTokenProvider;
import scrumpledpaper.agiler.common.utils.CookieUtils;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final AuthTokenProvider tokenProvider;
	private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
	private final OAuth2AuthenticationFailureHandler failureHandler;

	private static final String REDIRECT_URL = "/dashboard";

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
		try {
			String targetUrl = determineTargetUrl(request, response, authentication);

			if (response.isCommitted()) {
				logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
				return;
			}

			clearAuthenticationAttributes(request, response);
			getRedirectStrategy().sendRedirect(request, response, targetUrl);
		} catch (Exception ex) {
			logger.error("OAuth2 Authentication success handler error", ex);
			AuthenticationException authException = new OAuth2AuthenticationException(ex.toString());
			failureHandler.onAuthenticationFailure(request, response, authException);
		}
	}

	@Override
	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		Long userId = ((CustomOAuth2User) authentication.getPrincipal()).getUserId();
		String accessToken = tokenProvider.createToken(userId);

		CookieUtils.addCookie(response, "accessToken", accessToken, 24 * 60 * 60);
		return REDIRECT_URL;
	}

	private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
		super.clearAuthenticationAttributes(request);
		httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
	}

}
