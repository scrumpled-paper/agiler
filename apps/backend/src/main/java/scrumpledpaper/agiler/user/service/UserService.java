package scrumpledpaper.agiler.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.common.utils.AuthTokenProvider;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.image.repository.ImageRepository;
import scrumpledpaper.agiler.image.service.ImageService;
import scrumpledpaper.agiler.user.dto.TokenResponseDto;
import scrumpledpaper.agiler.user.dto.UserResDto;
import scrumpledpaper.agiler.user.dto.UserUpdateReqDto;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.mapper.UserMapper;
import scrumpledpaper.agiler.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserMapper userMapper;
	private final ImageService imageService;
	private final UserRepository userRepository;
	private final AuthTokenProvider authTokenProvider;
	private final ImageRepository imageRepository;

	public User findById(Long userId) {
		return userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
	}

	/*
	 * only swagger login
	 * */
	@Transactional
	public TokenResponseDto login(String email) {
		Image image = Image.builder()
				.objectKey("user")
				.url("")
				.build();
		Image savedImage = imageRepository.save(image);

		User user = userRepository.findByEmail(email).orElse(null);
		if (user == null) {
			user = userMapper.toEntity(email, "nickname", savedImage.getId());
			userRepository.save(user);
		}

		String accessToken = authTokenProvider.createToken(user.getId());
		String refreshToken = authTokenProvider.refreshToken(user.getId());

		return new TokenResponseDto(accessToken, refreshToken, "Bearer");
	}

	@Transactional(readOnly = true)
	public UserResDto getUser(long userId) {
		User user = findById(userId);
		Image image = imageService.findById(user.getImageId());

		return userMapper.toDto(user, image.getUrl());
	}

	@Transactional
	public void updateUser(long userId, UserUpdateReqDto userUpdateReqDto) {
		User user = findById(userId);

		user.update(
			userUpdateReqDto.email(),
			userUpdateReqDto.nickname()
		);
	}
}

