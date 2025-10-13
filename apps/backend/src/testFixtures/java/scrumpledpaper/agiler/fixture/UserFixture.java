package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.user.entity.User;

public class UserFixture {

	public static User createUser() {
		return User.builder()
			.email("test@test.com")
			.nickname("테스트유저")
			.imageId(1L)
			.build();
	}

	public static User createUser(Long imageId) {
		return User.builder()
			.email("test@test.com")
			.nickname("테스트유저")
			.imageId(imageId)
			.build();
	}
}
