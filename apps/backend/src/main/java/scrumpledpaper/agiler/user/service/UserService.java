package scrumpledpaper.agiler.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.utils.AuthTokenProvider;
import scrumpledpaper.agiler.image.service.ImageService;
import scrumpledpaper.agiler.user.dto.TokenResponseDto;
import scrumpledpaper.agiler.user.dto.UserDto;
import scrumpledpaper.agiler.user.dto.UserResDto;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.mapper.UserMapper;
import scrumpledpaper.agiler.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
	private final UserMapper userMapper;
	private final ImageService imageService;
	private final UserRepository userRepository;
	private final AuthTokenProvider authTokenProvider;

	public User findById(Long userId) { // todo
		return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
	}

	public TokenResponseDto login(String email) { // todo
		Long imageId = imageService.findById(1L);

		User user = userRepository.findByEmail(email);
		if (user == null) {
			user = userMapper.toEntity(email, "nickname", imageId);
			userRepository.save(user);
		}

		String accessToken = authTokenProvider.createToken(user.getId());
		String refreshToken = authTokenProvider.refreshToken(user.getId());

		return new TokenResponseDto(accessToken, refreshToken, "Bearer");
	}

	public UserResDto getUser(UserDto userDto) {
		String imageUrl = imageService.getImageUrl(userDto.getImageId());
		return userMapper.toDto(userDto, imageUrl);
	}
}

