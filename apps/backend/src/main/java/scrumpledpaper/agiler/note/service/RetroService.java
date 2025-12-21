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
import scrumpledpaper.agiler.note.dto.NoteCreateReqDto;
import scrumpledpaper.agiler.note.dto.RetroResDto;
import scrumpledpaper.agiler.note.entity.Retro;
import scrumpledpaper.agiler.note.entity.RetroProfile;
import scrumpledpaper.agiler.note.mapper.RetroMapper;
import scrumpledpaper.agiler.note.repository.RetroProfileRepository;
import scrumpledpaper.agiler.note.repository.RetroRepository;
import scrumpledpaper.agiler.project.dto.ProjectAccessContext;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.service.ProjectValidator;
import scrumpledpaper.agiler.template.entity.RetroTemplate;
import scrumpledpaper.agiler.template.service.RetroTemplateService;

@Service
@RequiredArgsConstructor
public class RetroService {
	private final ImageService imageService;
	private final RetroRepository retroRepository;
	private final RetroTemplateService retroTemplateService;
	private final RetroProfileRepository retroProfileRepository;
	private final ProjectValidator projectValidator;
	private final RetroMapper retroMapper;

	@Transactional(readOnly = true)
	public PageResDto<RetroResDto> getRetrospects(long userId, String projectUrl, Pageable pageable) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		Page<Retro> retroPage = retroRepository.findAllByProjectId(project.getId(), pageable);
		PageValidator.validatePageInRange(retroPage);

		if (retroPage.isEmpty()) {
			return PageResDto.empty(retroPage);
		}

		List<Long> retroIds = retroPage.stream()
			.map(Retro::getId)
			.toList();

		List<RetroProfile> retroProfiles = retroProfileRepository.findAllByRetroIdsWithProfile(retroIds);

		List<Long> imageIds = retroProfiles.stream()
			.map(RetroProfile::getProfile)
			.map(Profile::getImageId)
			.distinct()
			.toList();

		Map<Long, String> imageUrls = imageService.getImageUrlsByIds(imageIds);

		Map<Long, List<Profile>> participantMap = retroProfiles
			.stream()
			.collect(Collectors.groupingBy(
				retroProfile -> retroProfile.getRetro().getId(),
				Collectors.mapping(RetroProfile::getProfile, Collectors.toList())
			));

		Page<RetroResDto> dtoPage = retroPage.map(
			meeting -> retroMapper.toRetroResDto(
				meeting,
				participantMap.getOrDefault(meeting.getId(), List.of()),
				imageUrls
			)
		);

		return PageResDto.from(dtoPage);
	}

	@Transactional
	public long createRetrospect(long userId, String projectUrl, NoteCreateReqDto request) {
		ProjectAccessContext context = projectValidator.validateAccess(userId, projectUrl);
		Project project = context.project();

		Retro retro = createRetroEntity(project, request);
		retroRepository.save(retro);

		return retro.getId();
	}

	private Retro createRetroEntity(Project project, NoteCreateReqDto request) {
		if (request.templateId() != null) {
			RetroTemplate template = retroTemplateService.findById(request.templateId());
			return retroMapper.toEntity(project, template);
		}
		return retroMapper.toEntity(project, "", "");
	}
}
