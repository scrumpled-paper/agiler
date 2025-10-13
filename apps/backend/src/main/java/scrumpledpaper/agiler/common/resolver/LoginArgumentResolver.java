package scrumpledpaper.agiler.common.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.utils.AuthTokenProvider;
import scrumpledpaper.agiler.user.dto.UserDto;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.service.UserService;

@Component
@RequiredArgsConstructor
public class LoginArgumentResolver implements HandlerMethodArgumentResolver {

	private final UserService userService;
	private final AuthTokenProvider authTokenProvider;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		boolean hasLoginAnnotation = parameter.hasParameterAnnotation(Login.class);
		boolean hasUserDtoType = UserDto.class.isAssignableFrom(parameter.getParameterType());
		return hasLoginAnnotation && hasUserDtoType;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

		String token = request.getHeader("Authorization");
		if (token != null && token.startsWith("Bearer ")) {
			token = token.substring(7);
			
			if (!authTokenProvider.validateToken(token)) {
				throw new RuntimeException("Invalid token");
			}
			
			Long userId = authTokenProvider.getUserIdFromAccessToken(token);
			User user = userService.findById(userId);
			return UserDto.from(user);
		}
		throw new RuntimeException("No valid token");
	}
}
