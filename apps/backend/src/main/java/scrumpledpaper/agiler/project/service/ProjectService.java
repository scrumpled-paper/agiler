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
import scrumpledpaper.agiler.kanban.service.LabelService;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.dto.ProjectCheckReqDto;
import scrumpledpaper.agiler.project.dto.ProjectCheckResDto;
import scrumpledpaper.agiler.project.dto.ProjectCreateReqDto;
import scrumpledpaper.agiler.project.dto.ProjectDetailResDto;
import scrumpledpaper.agiler.project.dto.ProjectIdResDto;
import scrumpledpaper.agiler.project.dto.ProjectInfoResDto;
import scrumpledpaper.agiler.project.dto.ProjectSideResDto;
import scrumpledpaper.agiler.project.dto.ProjectUpdateReqDto;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.entity.Role;
import scrumpledpaper.agiler.project.mapper.ProjectMapper;
import scrumpledpaper.agiler.project.repository.ProjectRepository;
import scrumpledpaper.agiler.template.service.IssueTemplateService;
import scrumpledpaper.agiler.template.service.ScrumTemplateService;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.service.UserService;

@Service
@RequiredArgsConstructor
public class ProjectService {
	private final ProjectMapper projectMapper;
	private final UserService userService;
	private final ImageService imageService;
	private final LabelService labelService;
	private final ProfileService profileService;
	private final IssueTemplateService issueTemplateService;
	private final ScrumTemplateService scrumTemplateService;
	private final ProjectRepository projectRepository;
	private final ProjectValidator projectValidator;

	@Transactional
	public ProjectIdResDto createProject(long userId, ProjectCreateReqDto projectCreateReqDto) {
		User user = userService.findById(userId);

		if (alreadyExistProjectUrl(projectCreateReqDto.url())) {
			throw new CustomException(ErrorCode.PROJECT_URL_ALREADY_EXISTS);
		}

		Project savedProject = projectMapper.toEntity(projectCreateReqDto);
		projectRepository.save(savedProject);

		profileService.createDefaultProfile(user, savedProject, Role.OWNER);
		labelService.createDefaultLabels(savedProject);
		issueTemplateService.createDefaultIssueTemplates(savedProject);
		scrumTemplateService.createDefaultScrumTemplates(savedProject);
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
	public PageResDto<ProjectInfoResDto> getProjectInfo(long userId, Pageable pageable) {
		Page<ProjectInfoResDto> page = profileService
			.getProfilesByUserId(userId, pageable)
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
	public PageResDto<ProjectSideResDto> getProjectSide(long userId, Pageable pageable) {
		Page<ProjectSideResDto> page = profileService
			.getProfilesByUserId(userId, pageable)
			.map(Profile::getProject)
			.map(projectMapper::toProjectSideResDto);

		PageValidator.validatePageInRange(page);
		return PageResDto.from(page);
	}

	@Transactional(readOnly = true)
	public ProjectDetailResDto getProjectDetailByUrl(long userId, String projectUrl) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		String imageUrl = Optional.ofNullable(project.getImageId())
			.map(imageService::getImageUrlById)
			.orElse("");

		return projectMapper.toProjectDetailResDto(project, imageUrl);
	}

	@Transactional
	public ProjectIdResDto updateProjectDetailByUrl(long userId, String projectUrl,	ProjectUpdateReqDto projectUpdateReqDto) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		projectValidator.validateOwner(context.profile());
		Project project = context.project();

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
}
