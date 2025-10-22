package scrumpledpaper.agiler.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import scrumpledpaper.agiler.annotation.IntegrationTest;
import scrumpledpaper.agiler.fixture.UserFixture;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
public class UserTest {
	@Autowired
	private UserRepository userRepository;

	@Nested
	@DisplayName("User Entity HTable test")
	class HTableTest {
		@Test
		@DisplayName("200 - User HTable Success")
		void userCanDeleteIssueAndHTableWorksProperly() {
			// given
			User user = UserFixture.createUser();
			userRepository.save(user);

			// when
			userRepository.delete(user);
			userRepository.flush();

			// then
			Optional<User> deletedUser = userRepository.findById(user.getId());
			assertThat(deletedUser).isEmpty();
		}
	}
}
