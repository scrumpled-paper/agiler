package scrumpledpaper.agiler.project.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.PageResDto;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.project.dto.ProjectCheckReqDto;
import scrumpledpaper.agiler.project.dto.ProjectCheckResDto;
import scrumpledpaper.agiler.project.dto.ProjectCreateReqDto;
import scrumpledpaper.agiler.project.dto.ProjectCreateResDto;
import scrumpledpaper.agiler.project.dto.ProjectInfoResDto;
import scrumpledpaper.agiler.project.dto.ProjectSideResDto;
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
	private final UserService userService;
	private final ProjectMapper projectMapper;
	private final ProfileService profileService;
	private final ProjectRepository projectRepository;

	@Transactional
	public ProjectCreateResDto createProject(UserDto userDto, ProjectCreateReqDto projectCreateReqDto) {
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
			.map(projectMapper::toProjectInfoResDto);

		if (page.isEmpty() && page.getTotalElements() > 0) {
			throw new CustomException(ErrorCode.PAGE_NOT_FOUND);
		}

		return PageResDto.from(page);
	}

	@Transactional(readOnly = true)
	public PageResDto<ProjectSideResDto> getProjectSide(UserDto userDto, Pageable pageable) {
		Page<ProjectSideResDto> page = profileService
			.getProfilesByUserId(userDto.getId(), pageable)
			.map(Profile::getProject)
			.map(projectMapper::toProjectSideResDto);

		if (page.isEmpty() && page.getTotalElements() > 0) {
			throw new CustomException(ErrorCode.PAGE_NOT_FOUND);
		}

		return PageResDto.from(page);
	}

	@Transactional(readOnly = true)
	public ProjectDetailResDto getProjectDetailByUrl(UserDto userDto, String projectUrl) {
		Project project = projectRepository.findByUrl(projectUrl)
			.orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

		boolean teamMemberHasAccess = profileService.existsByUserIdAndProjectId(userDto.getId(), project.getId());
		if (!teamMemberHasAccess) {
			throw new CustomException(ErrorCode.PROJECT_NOT_MEMBER);
		}

		String imageUrl = Optional.ofNullable(project.getImageId())
			.map(imageService::getImageUrlById)
			.orElse("");

		return projectMapper.toProjectDetailResDto(project, imageUrl);
	}
}
