package scrumpledpaper.agiler.auth.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import scrumpledpaper.agiler.auth.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import scrumpledpaper.agiler.common.config.AppProperties;
import scrumpledpaper.agiler.common.utils.AuthTokenProvider;
import scrumpledpaper.agiler.common.utils.CookieUtils;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.repository.UserRepository;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static scrumpledpaper.agiler.auth.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final AuthTokenProvider tokenProvider;
	private final AppProperties appProperties;
	private final UserRepository userRepository;
	private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
		String targetUrl = determineTargetUrl(request, response, authentication);

		if (response.isCommitted()) {
			logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
			return;
		}

		clearAuthenticationAttributes(request, response);
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}

	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
				.map(Cookie::getValue);

		if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
			throw new IllegalArgumentException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication.");
		}

		String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

		OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
		String email = (String) oauth2User.getAttributes().get("email");

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

		String accessToken = tokenProvider.createToken(user.getId());

		return UriComponentsBuilder.fromUriString(targetUrl)
				.queryParam("token", accessToken)
				.build().toUriString();
	}

	protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
		super.clearAuthenticationAttributes(request);
		httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
	}

	private boolean isAuthorizedRedirectUri(String uri) {
		URI clientRedirectUri = URI.create(uri);

		return appProperties.getOauth2().getAuthorizedRedirectUris()
				.stream()
				.anyMatch(authorizedRedirectUri -> {
					URI authorizedURI = URI.create(authorizedRedirectUri);
					return authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
							&& authorizedURI.getPort() == clientRedirectUri.getPort();
				});
	}

}
