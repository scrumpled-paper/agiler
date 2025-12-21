package scrumpledpaper.agiler.note.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.common.PageResDto;
import scrumpledpaper.agiler.common.PageValidator;
import scrumpledpaper.agiler.image.service.ImageService;
import scrumpledpaper.agiler.note.dto.ScrumResDto;
import scrumpledpaper.agiler.note.entity.Scrum;
import scrumpledpaper.agiler.note.entity.ScrumProfile;
import scrumpledpaper.agiler.note.mapper.ScrumMapper;
import scrumpledpaper.agiler.note.repository.ScrumProfileRepository;
import scrumpledpaper.agiler.note.repository.ScrumRepository;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.service.ProjectValidator;
import scrumpledpaper.agiler.template.service.ScrumTemplateService;

@Service
@RequiredArgsConstructor
public class ScrumService {
	private final ImageService imageService;
	private final ScrumRepository scrumRepository;
	private final ScrumTemplateService scrumTemplateService;
	private final ScrumProfileRepository scrumProfileRepository;
	private final ScrumMapper scrumMapper;
	private final ProjectValidator projectValidator;

	@Transactional(readOnly = true)
	public PageResDto<ScrumResDto> getRetrospects(long userId, String projectUrl, Pageable pageable) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		Page<Scrum> scrumPage = scrumRepository.findAllByProjectId(project.getId(), pageable);
		PageValidator.validatePageInRange(scrumPage);

		if (scrumPage.isEmpty()) {
			return PageResDto.empty(scrumPage);
		}

		List<Long> scrumIds = scrumPage.stream()
			.map(Scrum::getId)
			.toList();

		List<ScrumProfile> scrumProfiles = scrumProfileRepository.findAllByScrumIdsWithProfile(scrumIds);

		List<Long> imageIds = scrumProfiles.stream()
			.map(ScrumProfile::getProfile)
			.map(Profile::getImageId)
			.distinct()
			.toList();

		Map<Long, String> imageUrls = imageService.getImageUrlsByIds(imageIds);

		Map<Long, List<Profile>> participantMap = scrumProfiles
			.stream()
			.collect(Collectors.groupingBy(
				scrumProfile -> scrumProfile.getScrum().getId(),
				Collectors.mapping(ScrumProfile::getProfile, Collectors.toList())
			));

		Page<ScrumResDto> dtoPage = scrumPage.map(
			scrum -> scrumMapper.toScrumResDto(
				scrum,
				participantMap.getOrDefault(scrum.getId(), List.of()),
				imageUrls
			)
		);

		return PageResDto.from(dtoPage);
	}
}
