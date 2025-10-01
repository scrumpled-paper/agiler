package scrumpledpaper.agiler.user;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.repository.UserRepository;

@SpringBootTest
public class UserTest {
	@Autowired
	private UserRepository userRepository;

	@Test
	@Transactional
	void userCanDeleteIssueAndHTableWorksProperly() throws Exception {
		// given
		User testUser = new User();
		setField(testUser, "vendor", "testVendor");
		setField(testUser, "vendorId", "testVendorId");
		setField(testUser, "nickname", "testNick");
		setField(testUser, "imgId", 1L);
		userRepository.save(testUser);

		// when
		userRepository.delete(testUser);
		userRepository.flush();

		// then
		Optional<User> deletedUser = userRepository.findById(testUser.getId());
		assertThat(deletedUser).isEmpty();
	}


	private void setField(Object target, String fieldName, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}
}
