package scrumpledpaper.agiler.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import scrumpledpaper.agiler.auth.service.CustomUserDetailsService;
import scrumpledpaper.agiler.common.utils.AuthTokenProvider;
import scrumpledpaper.agiler.common.utils.CookieUtils;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final AuthTokenProvider authTokenProvider;
	private final CustomUserDetailsService customUserDetailsService;

	private static final String ACCESS_TOKEN = "accessToken";

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain
	) throws ServletException, IOException {
		String token = resolveToken(request);

		if (StringUtils.hasText(token) && authTokenProvider.validateToken(token)) {
			String userId = authTokenProvider.getUserIdFromAccessToken(token);
			UserDetails userDetails = customUserDetailsService.loadUserByUsername(userId);

			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					userDetails, null, userDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		filterChain.doFilter(request, response);
	}

	private String resolveToken(HttpServletRequest request) {
		return CookieUtils.getCookie(request, ACCESS_TOKEN)
				.map(Cookie::getValue)
				.orElse(null);
	}

}
