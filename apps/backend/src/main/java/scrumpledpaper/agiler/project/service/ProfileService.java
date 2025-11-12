package scrumpledpaper.agiler.project.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.image.service.ImageService;
import scrumpledpaper.agiler.project.dto.ProfileResDto;
import scrumpledpaper.agiler.project.dto.ProfileRoleUpdateReqDto;
import scrumpledpaper.agiler.project.dto.ProfileUpdateReqDto;
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

	public Page<Profile> getProfilesByUserId(long userId, Pageable pageable) {
		return profileRepository.findByUserId(userId, pageable);
	}

	public boolean existsByUserIdAndProjectId(long userId, long projectId) {
		return profileRepository.existsByUserIdAndProjectId(userId, projectId);
	}

	public Profile getProfileByUserIdAndProjectId(long userId, long projectId) {
		return profileRepository.findByUserIdAndProjectId(userId, projectId)
			.orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_MEMBER));
	}

	public Profile getProfileByProfileIdAndProjectId(long profileId, long projectId) {
		return profileRepository.findByIdAndProjectId(profileId, projectId)
			.orElseThrow(() -> new CustomException(ErrorCode.PROJECT_PROFILE_NOT_FOUND));
	}

	public Page<ProfileResDto> getProfileResDtosByProjectId(long projectId, Pageable pageable) {
		return profileRepository.findByProjectId(projectId, pageable)
			.map(profile -> {
				String imageUrl = Optional.ofNullable(profile.getUser().getImageId())
					.map(imageService::getImageUrlById)
					.orElse("");

				return profileMapper.toProfileResDto(profile, imageUrl);
			});
	}

	public ProfileResDto getProjectProfileResDto(long profileId, long projectId) {
		Profile profile = getProfileByProfileIdAndProjectId(profileId, projectId);

		String imageUrl = Optional.ofNullable(profile.getUser().getImageId())
			.map(imageService::getImageUrlById)
			.orElse("");

		return profileMapper.toProfileResDto(profile, imageUrl);
	}

	public ProfileResDto getMyProjectProfileResDto(long userId, long projectId) {
		Profile profile = getProfileByUserIdAndProjectId(userId, projectId);

		String imageUrl = Optional.ofNullable(profile.getUser().getImageId())
			.map(imageService::getImageUrlById)
			.orElse("");

		return profileMapper.toProfileResDto(profile, imageUrl);
	}

	public void updateProfile(long userId, long projectId, ProfileUpdateReqDto profileUpdateReqDto) {
		Profile profile = getProfileByUserIdAndProjectId(userId, projectId);

		profile.updateDetails(
			profileUpdateReqDto.nickname(),
			profileUpdateReqDto.email(),
			profileUpdateReqDto.description()
		);
	}

	public void ensureOwnerRemainsInProject(long targetProfileId, long projectId, Role role) {
		if (role == Role.OWNER) {
			return;
		}
		long remainingOwnerCount = profileRepository.countByProjectIdAndRoleAndIdNot(projectId, Role.OWNER, targetProfileId);
		if (remainingOwnerCount <= 0) {
			throw new CustomException(ErrorCode.PROJECT_OWNER_MINIMUM_REQUIRED);
		}
	}

	public void updateProfileRole(ProfileRoleUpdateReqDto profileRoleUpdateReqDto, long projectId) {
		long targetProfileId = profileRoleUpdateReqDto.profileId();
		Profile profile = getProfileByProfileIdAndProjectId(targetProfileId, projectId);
		Role newRole = Role.from(profileRoleUpdateReqDto.role());

		ensureOwnerRemainsInProject(targetProfileId, projectId, newRole);
		profile.updateRole(newRole);
	}
}
