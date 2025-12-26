package scrumpledpaper.agiler.auth.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

	@Value("${app.api.key}")
	private String expectedKey;
	private static final String INTERNAL_API_ROLE = "ROLE_INTERNAL";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		String path = request.getRequestURI();
		if (path.startsWith("/internal/")) {
			String key = request.getHeader("X-API-KEY");
			if (expectedKey.equals(key)) {
				Authentication authentication = new PreAuthenticatedAuthenticationToken(
					key,
					null,
					List.of(new SimpleGrantedAuthority(INTERNAL_API_ROLE))
				);
				SecurityContextHolder.getContext().setAuthentication(authentication);
				filterChain.doFilter(request, response);
			} else {
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				response.getWriter().write("Invalid API Key");
			}
		} else {
			filterChain.doFilter(request, response);
		}
	}
}
