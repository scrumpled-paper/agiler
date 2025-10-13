package scrumpledpaper.agiler.user.mapper;

import org.mapstruct.Mapper;

import scrumpledpaper.agiler.user.dto.UserDto;
import scrumpledpaper.agiler.user.dto.UserResDto;
import scrumpledpaper.agiler.user.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
	User toEntity(String email, String nickname, Long imageId);

	UserResDto toDto(UserDto userDto, String imageUrl);
}
