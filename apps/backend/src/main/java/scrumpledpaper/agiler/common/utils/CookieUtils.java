package scrumpledpaper.agiler.common.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.util.SerializationUtils;

import java.util.Base64;
import java.util.Optional;

public class CookieUtils {

	public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return Optional.of(cookie);
				}
			}
		}

		return Optional.empty();
	}

	public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
		ResponseCookie cookie = ResponseCookie.from(name, value)
				.path("/")
				.httpOnly(true)
				.maxAge(maxAge)
				.sameSite("Lax")
				.build();
		response.addHeader("Set-Cookie", cookie.toString());
	}

	public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					cookie.setValue("");
					cookie.setPath("/");
					cookie.setMaxAge(0);
					response.addCookie(cookie);
				}
			}
		}
	}

	public static String serialize(Object object) {
		return Base64.getUrlEncoder()
				.encodeToString(SerializationUtils.serialize(object));
	}

	public static <T> T deserialize(Cookie cookie, Class<T> cls) {
		return cls.cast(SerializationUtils.deserialize(
				Base64.getUrlDecoder().decode(cookie.getValue())));
	}

	public static void addProjectUrlCookie(HttpServletResponse response, String projectUrl, long maxAgeSeconds) {
		ResponseCookie cookie = ResponseCookie.from("project_url", projectUrl)
			.path("/")
			.httpOnly(true)
			.maxAge(maxAgeSeconds)
			.sameSite("Lax")
			.build();
		response.addHeader("Set-Cookie", cookie.toString());
	}
}
