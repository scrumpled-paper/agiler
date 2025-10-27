package scrumpledpaper.agiler.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import scrumpledpaper.agiler.user.entity.User;

@Getter
@AllArgsConstructor
public class AuthContext {
	private User user;
	private String token;

	public String bearer() {
		return "Bearer " + token;
	}
}
