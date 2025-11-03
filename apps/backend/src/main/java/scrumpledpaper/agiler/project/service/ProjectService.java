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
import scrumpledpaper.agiler.project.dto.ProjectCheckReqDto;
import scrumpledpaper.agiler.project.dto.ProjectCheckResDto;
import scrumpledpaper.agiler.project.dto.ProjectCreateReqDto;
import scrumpledpaper.agiler.project.dto.ProjectDetailResDto;
import scrumpledpaper.agiler.project.dto.ProjectIdResDto;
import scrumpledpaper.agiler.project.dto.ProjectInfoResDto;
import scrumpledpaper.agiler.project.dto.ProjectSideResDto;
import scrumpledpaper.agiler.project.dto.ProjectUpdateReqDto;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.mapper.ProjectMapper;
import scrumpledpaper.agiler.project.repository.ProjectRepository;
import scrumpledpaper.agiler.user.dto.UserDto;
import scrumpledpaper.agiler.user.entity.Profile;
import scrumpledpaper.agiler.user.entity.Role;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.service.ProfileService;
import scrumpledpaper.agiler.user.service.UserService;

@Service
@RequiredArgsConstructor
public class ProjectService {
	private final ProjectMapper projectMapper;
	private final UserService userService;
	private final ImageService imageService;
	private final ProfileService profileService;
	private final ProjectRepository projectRepository;

	@Transactional
	public ProjectIdResDto createProject(UserDto userDto, ProjectCreateReqDto projectCreateReqDto) {
		User user = userService.findById(userDto.getId());

		if (alreadyExistProjectUrl(projectCreateReqDto.url())) {
			throw new CustomException(ErrorCode.PROJECT_URL_ALREADY_EXISTS);
		}

		Project savedProject = projectMapper.toEntity(projectCreateReqDto);
		projectRepository.save(savedProject);

		profileService.createDefaultProfile(user, savedProject, Role.OWNER);
		return projectMapper.toDto(savedProject);
	}

	@Transactional(readOnly = true)
	public ProjectCheckResDto checkProjectUrl(ProjectCheckReqDto projectCheckReqDto) {
		boolean isDuplicated = alreadyExistProjectUrl(projectCheckReqDto.url());
		return new ProjectCheckResDto(isDuplicated);
	}

	private boolean alreadyExistProjectUrl(String url) {
		return projectRepository.existsByUrl(url);
	}

	@Transactional(readOnly = true)
	public PageResDto<ProjectInfoResDto> getProjectInfo(UserDto userDto, Pageable pageable) {
		Page<ProjectInfoResDto> page = profileService
			.getProfilesByUserId(userDto.getId(), pageable)
			.map(Profile::getProject)
			.map(project -> {
				String imageUrl = Optional.ofNullable(project.getImageId())
					.map(imageService::getImageUrlById)
					.orElse("");

				return projectMapper.toProjectInfoResDto(project, imageUrl);
			});

		PageValidator.validatePageInRange(page);
		return PageResDto.from(page);
	}

	@Transactional(readOnly = true)
	public PageResDto<ProjectSideResDto> getProjectSide(UserDto userDto, Pageable pageable) {
		Page<ProjectSideResDto> page = profileService
			.getProfilesByUserId(userDto.getId(), pageable)
			.map(Profile::getProject)
			.map(projectMapper::toProjectSideResDto);

		PageValidator.validatePageInRange(page);
		return PageResDto.from(page);
	}

	@Transactional(readOnly = true)
	public ProjectDetailResDto getProjectDetailByUrl(UserDto userDto, String projectUrl) {
		Project project = findProjectByUrl(projectUrl);
		validateProjectAccess(userDto.getId(), project.getId());

		String imageUrl = Optional.ofNullable(project.getImageId())
			.map(imageService::getImageUrlById)
			.orElse("");

		return projectMapper.toProjectDetailResDto(project, imageUrl);
	}

	@Transactional
	public ProjectIdResDto updateProjectDetailByUrl(UserDto userDto, String projectUrl,	ProjectUpdateReqDto projectUpdateReqDto) {
		Project project = findProjectByUrl(projectUrl);
		validateProjectOwnerAccess(userDto.getId(), project.getId());

		if (!project.getUrl().equals(projectUpdateReqDto.url()) &&
			alreadyExistProjectUrl(projectUpdateReqDto.url())) {
			throw new CustomException(ErrorCode.PROJECT_URL_ALREADY_EXISTS);
		}

		project.updateDetails(
			projectUpdateReqDto.title(),
			projectUpdateReqDto.url(),
			projectUpdateReqDto.summary()
		);

		return projectMapper.toDto(project);
	}

	private Project findProjectByUrl(String projectUrl) {
		return projectRepository.findByUrl(projectUrl)
			.orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
	}

	private void validateProjectAccess(Long userId, Long projectId) {
		if (!profileService.existsByUserIdAndProjectId(userId, projectId)) {
			throw new CustomException(ErrorCode.PROJECT_NOT_MEMBER);
		}
	}

	private void validateProjectOwnerAccess(Long userId, Long projectId) {
		Profile profile = profileService.getProfileByUserIdAndProjectId(userId, projectId);
		if (profile.getRole() != Role.OWNER) {
			throw new CustomException(ErrorCode.PROJECT_OWNER_REQUIRED);
		}
	}

	@Transactional(readOnly = true)
	public PageResDto<ProfileResDto> getProjectMembersByUrl(UserDto userDto, String projectUrl, Pageable pageable) {
		Project project = findProjectByUrl(projectUrl);
		validateProjectAccess(userDto.getId(), project.getId());

		Page<ProfileResDto> page = profileService.getProfileResDtosByProjectId(project.getId(), pageable);

		PageValidator.validatePageInRange(page);
		return PageResDto.from(page);
	}
}
