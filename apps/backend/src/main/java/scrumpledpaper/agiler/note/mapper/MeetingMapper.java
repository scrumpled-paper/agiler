package scrumpledpaper.agiler.note.mapper;

import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import scrumpledpaper.agiler.note.dto.MeetingResDto;
import scrumpledpaper.agiler.note.entity.Meeting;
import scrumpledpaper.agiler.note.entity.MeetingProfile;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.template.entity.MeetingTemplate;

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

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "title", source = "template.title")
	@Mapping(target = "contents", source = "template.contents")
	@Mapping(target = "project", source = "project")
	@Mapping(target = "meetingProfiles", ignore = true)
	Meeting toEntity(Project project, MeetingTemplate template);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "title", source = "title")
	@Mapping(target = "contents", source = "contents")
	@Mapping(target = "project", source = "project")
	@Mapping(target = "meetingProfiles", ignore = true)
	Meeting toEntity(Project project, String title, String contents);

	@Mapping(target = "id", ignore = true)
	MeetingProfile toMeetingProfileEntity(Meeting meeting, Profile profile);
}
