package scrumpledpaper.agiler.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.common.utils.AuthTokenProvider;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.image.service.ImageService;
import scrumpledpaper.agiler.user.dto.TokenResponseDto;
import scrumpledpaper.agiler.user.dto.UserDto;
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

	public User findById(Long userId) { // todo
		return userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
	}

	@Transactional
	public TokenResponseDto login(String email) { // todo
		Image image = imageService.findById(1L);

		User user = userRepository.findByEmail(email);
		if (user == null) {
			user = userMapper.toEntity(email, "nickname", image.getId());
			userRepository.save(user);
		}

		String accessToken = authTokenProvider.createToken(user.getId());
		String refreshToken = authTokenProvider.refreshToken(user.getId());

		return new TokenResponseDto(accessToken, refreshToken, "Bearer");
	}

	@Transactional(readOnly = true)
	public UserResDto getUser(UserDto userDto) {
		Image image = imageService.findById(userDto.getImageId());
		return userMapper.toDto(userDto, image.getUrl());
	}

	@Transactional
	public void updateUser(UserDto userDto, UserUpdateReqDto userUpdateReqDto) {
		User user = findById(userDto.getId());
		user.updateNickname(userUpdateReqDto.nickname());
	}
}

