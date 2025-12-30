package scrumpledpaper.agiler.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findById(Long.parseLong(username))
			.orElseThrow(() -> new UsernameNotFoundException("해당하는 유저를 찾을 수 없습니다. ID: " + username));
		return createUserDetails(user);
	}

	private UserDetails createUserDetails(User user) {
		return new CustomUserDetails(user.getId());
	}

}
