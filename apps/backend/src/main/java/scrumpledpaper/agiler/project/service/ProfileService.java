package scrumpledpaper.agiler.project.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.image.service.ImageService;
import scrumpledpaper.agiler.project.dto.ProfileResDto;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.entity.Role;
import scrumpledpaper.agiler.project.mapper.ProfileMapper;
import scrumpledpaper.agiler.project.repository.ProfileRepository;
import scrumpledpaper.agiler.user.entity.User;

@Service
@RequiredArgsConstructor
public class ProfileService {
	private final ProfileMapper profileMapper;
	private final ImageService imageService;
	private final ProfileRepository profileRepository;

	public void createDefaultProfile(User user, Project savedProject, Role role) {
		Profile defaultProfile = profileMapper.toEntity(user, savedProject, role);
		profileRepository.save(defaultProfile);
	}

	public Page<Profile> getProfilesByUserId(Long userId, Pageable pageable) {
		return profileRepository.findByUserId(userId, pageable);
	}

	public boolean existsByUserIdAndProjectId(Long userId, Long projectId) {
		return profileRepository.existsByUserIdAndProjectId(userId, projectId);
	}

	public Profile getProfileByUserIdAndProjectId(Long userId, Long projectId) {
		return profileRepository.findByUserIdAndProjectId(userId, projectId)
			.orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_MEMBER));
	}

	public Page<ProfileResDto> getProfileResDtosByProjectId(Long projectId, Pageable pageable) {
		return profileRepository.findByProjectId(projectId, pageable)
			.map(profile -> {
				String imageUrl = Optional.ofNullable(profile.getUser().getImageId())
					.map(imageService::getImageUrlById)
					.orElse("");

				return profileMapper.toProfileResDto(profile, imageUrl);
			});
	}

	public ProfileResDto getProjectProfileResDto(long profileId, long projectId) {
		Profile profile = getProfileByUserIdAndProjectId(profileId, projectId);

		String imageUrl = Optional.ofNullable(profile.getUser().getImageId())
			.map(imageService::getImageUrlById)
			.orElse("");

		return profileMapper.toProfileResDto(profile, imageUrl);
	}
}
