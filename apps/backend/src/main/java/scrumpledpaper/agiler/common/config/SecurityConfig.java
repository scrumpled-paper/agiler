package scrumpledpaper.agiler.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import scrumpledpaper.agiler.auth.filter.ApiKeyAuthFilter;
import scrumpledpaper.agiler.auth.filter.JwtAuthenticationFilter;
import scrumpledpaper.agiler.auth.handler.CustomAccessDeniedHandler;
import scrumpledpaper.agiler.auth.handler.CustomAuthenticationEntryPoint;
import scrumpledpaper.agiler.auth.handler.OAuth2AuthenticationFailureHandler;
import scrumpledpaper.agiler.auth.handler.OAuth2AuthenticationSuccessHandler;
import scrumpledpaper.agiler.auth.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import scrumpledpaper.agiler.auth.service.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final ApiKeyAuthFilter apiKeyAuthFilter;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	private final CustomAccessDeniedHandler customAccessDeniedHandler;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
	private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
	private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

	private static final String INTERNAL_API_ROLE = "ROLE_INTERNAL";
	private static final String[] PERMIT_URL_ARRAY = {
			/* swagger */
			"/v3/api-docs/**",
			"/swagger-ui/**",
			/* auth */
			"/api/v1/login/**",
			/* health check */
			"/actuator/**"
	};

	@Bean
	@Order(1)
	public SecurityFilterChain internalSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.securityMatcher("/internal/**")
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint(customAuthenticationEntryPoint)
				.accessDeniedHandler(customAccessDeniedHandler)
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/internal/**").hasAuthority(INTERNAL_API_ROLE)
				.anyRequest().authenticated()
			)
			.addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
	@Bean
	@Order(2)
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.securityMatcher(request -> !request.getRequestURI().startsWith("/internal/"))
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint(customAuthenticationEntryPoint)
				.accessDeniedHandler(customAccessDeniedHandler)
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(PERMIT_URL_ARRAY).permitAll()
				.anyRequest().authenticated()
			)
			.oauth2Login(oauth2 -> oauth2
				.authorizationEndpoint(auth -> auth
					.baseUri("/api/oauth2/authorization")
					.authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
				)
				.redirectionEndpoint(redirect -> redirect
					.baseUri("/api/login/oauth2/code/*")
				)
				.userInfoEndpoint(userInfo -> userInfo
					.userService(customOAuth2UserService)
				)
				.successHandler(oAuth2AuthenticationSuccessHandler)
				.failureHandler(oAuth2AuthenticationFailureHandler)
			)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
