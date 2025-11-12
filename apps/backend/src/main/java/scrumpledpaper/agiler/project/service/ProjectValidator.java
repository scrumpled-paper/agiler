package scrumpledpaper.agiler.project.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.entity.Role;
import scrumpledpaper.agiler.project.repository.ProfileRepository;
import scrumpledpaper.agiler.project.repository.ProjectRepository;

@Service
@RequiredArgsConstructor
public class ProjectValidator {
	private final ProjectRepository projectRepository;
	private final ProfileRepository profileRepository;

	@Transactional(readOnly = true)
	public ProjectAccessContext validateAccess(long userId, String projectUrl) {
		Project project = projectRepository.findByUrl(projectUrl)
			.orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

		Profile profile = profileRepository.findByUserIdAndProjectId(userId, project.getId())
			.orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_MEMBER));

		return new ProjectAccessContext(project, profile);
	}

	public void validateOwner(Profile profile) {
		if (profile.getRole() != Role.OWNER) {
			throw new CustomException(ErrorCode.PROJECT_OWNER_REQUIRED);
		}
	}
}
