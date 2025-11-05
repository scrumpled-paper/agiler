package scrumpledpaper.agiler.user.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.image.service.ImageService;
import scrumpledpaper.agiler.project.dto.ProfileResDto;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.repository.ProjectRepository;
import scrumpledpaper.agiler.user.entity.Profile;
import scrumpledpaper.agiler.user.entity.Role;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.mapper.ProfileMapper;
import scrumpledpaper.agiler.user.repository.ProfileRepository;

@Service
@RequiredArgsConstructor
public class ProfileService {
	private final ProfileMapper profileMapper;
	private final ImageService imageService;
	private final ProjectRepository projectRepository;
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

	public ProfileResDto getProjectProfile(Long id, String ProjectUrl) {
		Project project = projectRepository.findByUrl(ProjectUrl)
			.orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
		Profile profile = getProfileByUserIdAndProjectId(id, project.getId());

		String imageUrl = Optional.ofNullable(profile.getUser().getImageId())
			.map(imageService::getImageUrlById)
			.orElse("");

		return profileMapper.toProfileResDto(profile, imageUrl);
	}
}
