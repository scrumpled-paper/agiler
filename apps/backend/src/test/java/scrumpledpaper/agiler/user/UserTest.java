package scrumpledpaper.agiler.user;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import scrumpledpaper.agiler.fixture.UserFixture;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.repository.UserRepository;

@SpringBootTest
@Transactional
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
