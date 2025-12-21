package scrumpledpaper.agiler.note.mapper;

import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import scrumpledpaper.agiler.note.dto.ScrumResDto;
import scrumpledpaper.agiler.note.entity.Scrum;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.template.entity.ScrumTemplate;

@Mapper(componentModel = "spring")
public interface ScrumMapper {
	@Mapping(target = "participants", ignore = true)
	@Mapping(target = "scrumId", source = "id")
	ScrumResDto toScrumResDto(Scrum scrum);

	default ScrumResDto.ParticipantResDto toParticipantResDto(Profile profile, String imageUrl) {
		return new ScrumResDto.ParticipantResDto(
			profile.getId(),
			profile.getNickname(),
			imageUrl
		);
	}

	default ScrumResDto toScrumResDto(Scrum scrum, List<Profile> participants, Map<Long, String> imageUrls) {
		ScrumResDto base = toScrumResDto(scrum);

		ScrumResDto.ParticipantResDto[] participantDtos = participants.stream()
			.map(profile -> {
				String imageUrl = imageUrls.get(profile.getImageId());
				return toParticipantResDto(profile, imageUrl);
			})
			.toArray(ScrumResDto.ParticipantResDto[]::new);

		return new ScrumResDto(
			base.scrumId(),
			base.title(),
			base.createdAt(),
			participantDtos
		);
	}

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "title", source = "template.title")
	@Mapping(target = "contents", source = "template.contents")
	@Mapping(target = "project", source = "project")
	@Mapping(target = "scrumProfiles", ignore = true)
	Scrum toEntity(Project project, ScrumTemplate template);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "title", source = "title")
	@Mapping(target = "contents", source = "contents")
	@Mapping(target = "project", source = "project")
	@Mapping(target = "scrumProfiles", ignore = true)
	Scrum toEntity(Project project, String title, String contents);
}
