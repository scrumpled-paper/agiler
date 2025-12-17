package scrumpledpaper.agiler.project.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import scrumpledpaper.agiler.kanban.dto.KanbanBoardResDto;
import scrumpledpaper.agiler.project.dto.ProfileResDto;
import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.project.entity.Role;
import scrumpledpaper.agiler.user.entity.User;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "user", source = "user")
	@Mapping(target = "imageId", source = "user.imageId")
	Profile toEntity(User user, Project project, Role role);

	@Mapping(target = "profileId", source = "profile.id")
	@Mapping(target = "nickname", source = "profile.nickname")
	@Mapping(target = "email", source = "profile.email")
	@Mapping(target = "description", source = "profile.description")
	ProfileResDto toProfileResDto(Profile profile, String imageUrl);

	default KanbanBoardResDto.ProfileDto toKanbanProfileDto(Profile profile, String imageUrl) {
		return new KanbanBoardResDto.ProfileDto(
			profile.getId(),
			profile.getNickname(),
			profile.getEmail(),
			imageUrl,
			profile.getRole().name(),
			profile.getDescription()
		);
	}
}
