package scrumpledpaper.agiler.note.mapper;

import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import scrumpledpaper.agiler.note.dto.MeetingResDto;
import scrumpledpaper.agiler.note.entity.Meeting;
import scrumpledpaper.agiler.project.entity.Profile;

@Mapper(componentModel = "spring")
public interface MeetingMapper {
	@Mapping(target = "participants", ignore = true)
	@Mapping(target = "meetingId", source = "id")
	MeetingResDto toMeetingResDto(Meeting meeting);

	default MeetingResDto.ParticipantResDto toParticipantResDto(Profile profile, String imageUrl) {
		return new MeetingResDto.ParticipantResDto(
			profile.getId(),
			profile.getNickname(),
			imageUrl
		);
	}

	default MeetingResDto toMeetingResDto(Meeting meeting, List<Profile> participants, Map<Long, String> imageUrls) {
		MeetingResDto base = toMeetingResDto(meeting);

		MeetingResDto.ParticipantResDto[] participantDtos = participants.stream()
			.map(profile -> {
				String imageUrl = imageUrls.get(profile.getImageId());
				return toParticipantResDto(profile, imageUrl);
			})
			.toArray(MeetingResDto.ParticipantResDto[]::new);

		return new MeetingResDto(
			base.meetingId(),
			base.title(),
			base.createdAt(),
			participantDtos
		);
	}
}
