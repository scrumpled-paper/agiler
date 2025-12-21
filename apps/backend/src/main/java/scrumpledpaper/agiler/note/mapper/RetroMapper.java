package scrumpledpaper.agiler.note.mapper;

import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import scrumpledpaper.agiler.note.dto.RetroResDto;
import scrumpledpaper.agiler.note.entity.Retro;
import scrumpledpaper.agiler.project.entity.Profile;

@Mapper(componentModel = "spring")
public interface RetroMapper {
	@Mapping(target = "participants", ignore = true)
	@Mapping(target = "retroId", source = "id")
	RetroResDto toRetroResDto(Retro retro);

	default RetroResDto.ParticipantResDto toParticipantResDto(Profile profile, String imageUrl) {
		return new RetroResDto.ParticipantResDto(
			profile.getId(),
			profile.getNickname(),
			imageUrl
		);
	}

	default RetroResDto toRetroResDto(Retro retro, List<Profile> participants, Map<Long, String> imageUrls) {
		RetroResDto base = toRetroResDto(retro);

		RetroResDto.ParticipantResDto[] participantDtos = participants.stream()
			.map(profile -> {
				String imageUrl = imageUrls.get(profile.getImageId());
				return toParticipantResDto(profile, imageUrl);
			})
			.toArray(RetroResDto.ParticipantResDto[]::new);

		return new RetroResDto(
			base.retroId(),
			base.title(),
			base.createdAt(),
			participantDtos
		);
	}
}
