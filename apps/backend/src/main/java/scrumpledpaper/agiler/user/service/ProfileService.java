package scrumpledpaper.agiler.user.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.user.dto.UserResDto;
import scrumpledpaper.agiler.user.repository.ProfileRepository;

@Service
@RequiredArgsConstructor
public class ProfileService {
	private final ProfileRepository profileRepository;

	public UserResDto getProfile(Long userId) {
		return null;
	}
}
