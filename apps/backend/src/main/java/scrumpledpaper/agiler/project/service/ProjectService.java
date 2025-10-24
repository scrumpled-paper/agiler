package scrumpledpaper.agiler.project.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;
import scrumpledpaper.agiler.project.dto.ProjectCheckReqDto;
import scrumpledpaper.agiler.project.dto.ProjectCheckResDto;
import scrumpledpaper.agiler.project.dto.ProjectCreateReqDto;
import scrumpledpaper.agiler.project.dto.ProjectCreateResDto;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.mapper.ProjectMapper;
import scrumpledpaper.agiler.project.repository.ProjectRepository;
import scrumpledpaper.agiler.user.dto.UserDto;
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

		profileService.createDefaultProfile(user, savedProject, Role.owner);
		return projectMapper.toDto(savedProject);
	}

	public ProjectCheckResDto checkProjectUrl(ProjectCheckReqDto projectCheckReqDto) {
		boolean isDuplicated = alreadyExistProjectUrl(projectCheckReqDto.url());
		return new ProjectCheckResDto(isDuplicated);
	}

	private boolean alreadyExistProjectUrl(String url) {
		return projectRepository.existsByUrl(url);
	}
}
