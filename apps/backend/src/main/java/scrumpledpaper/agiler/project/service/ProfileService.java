package scrumpledpaper.agiler.project.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.PageResDto;
import scrumpledpaper.agiler.common.PageValidator;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.image.service.ImageService;
import scrumpledpaper.agiler.project.dto.ProfileResDto;
import scrumpledpaper.agiler.project.dto.ProfileUpdateReqDto;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
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
	private final ProjectValidator projectValidator;

	public void createDefaultProfile(User user, Project savedProject, Role role) {
		Profile defaultProfile = profileMapper.toEntity(user, savedProject, role);
		profileRepository.save(defaultProfile);
	}

	public Page<Profile> getProfilesByUserId(long userId, Pageable pageable) {
		return profileRepository.findByUserId(userId, pageable);
	}

	public Profile getProfileByProfileIdAndProjectId(long profileId, long projectId) {
		return profileRepository.findByIdAndProjectId(profileId, projectId)
			.orElseThrow(() -> new CustomException(ErrorCode.PROJECT_PROFILE_NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public PageResDto<ProfileResDto> getProjectMembersByUrl(long userId, String projectUrl, Pageable pageable) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		Page<ProfileResDto> page = profileRepository.findByProjectId(project.getId(), pageable)
			.map(profile -> {
				String imageUrl = Optional.ofNullable(profile.getUser().getImageId())
					.map(imageService::getImageUrlById)
					.orElse("");

				return profileMapper.toProfileResDto(profile, imageUrl);
			});

		PageValidator.validatePageInRange(page);
		return PageResDto.from(page);
	}

	@Transactional(readOnly = true)
	public ProfileResDto getProjectProfileById(long userId, String projectUrl, Long targetProfileId) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();
		Profile targetProfile = getProfileByProfileIdAndProjectId(targetProfileId, project.getId());

		String imageUrl = Optional.ofNullable(targetProfile.getUser().getImageId())
			.map(imageService::getImageUrlById)
			.orElse("");

		return profileMapper.toProfileResDto(targetProfile, imageUrl);
	}

	@Transactional(readOnly = true)
	public ProfileResDto getMyProjectProfile(Long userId, String projectUrl) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Profile profile = context.profile();

		String imageUrl = Optional.ofNullable(profile.getUser().getImageId())
			.map(imageService::getImageUrlById)
			.orElse("");

		return profileMapper.toProfileResDto(profile, imageUrl);
	}

	@Transactional
	public void updateProfile(long userId, String projectUrl, ProfileUpdateReqDto profileUpdateReqDto) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Profile profile = context.profile();

		profile.updateDetails(
			profileUpdateReqDto.nickname(),
			profileUpdateReqDto.email(),
			profileUpdateReqDto.description()
		);
	}

	@Transactional
	public void updateProfileRole(long userId, String projectUrl, long targetProfileId, String newRole) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		projectValidator.validateOwner(context.profile());
		Project project = context.project();
		Role role = Role.from(newRole);

		Profile targetProfile = getProfileByProfileIdAndProjectId(targetProfileId, project.getId());
		ensureOwnerRemainsInProject(targetProfileId, project.getId(), role);

		targetProfile.updateRole(role);
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
}
