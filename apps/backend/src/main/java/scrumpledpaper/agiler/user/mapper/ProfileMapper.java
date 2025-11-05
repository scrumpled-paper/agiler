package scrumpledpaper.agiler.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import scrumpledpaper.agiler.project.dto.ProfileResDto;
import scrumpledpaper.agiler.project.dto.ProjectSideResDto;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.user.entity.Profile;
import scrumpledpaper.agiler.user.entity.Role;
import scrumpledpaper.agiler.user.entity.User;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "user", source = "user")
	@Mapping(target = "imageId", source = "user.imageId")
	Profile toEntity(User user, Project project, Role role);

	@Mapping(target = "memberId", source = "profile.id")
	@Mapping(target = "nickname", source = "profile.nickname")
	@Mapping(target = "email", source = "profile.email")
	@Mapping(target = "description", source = "profile.description")
	ProfileResDto toProfileResDto(Profile profile, String imageUrl);
}
