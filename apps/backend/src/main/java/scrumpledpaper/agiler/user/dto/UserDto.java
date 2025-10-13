package scrumpledpaper.agiler.user.dto;

import lombok.Builder;
import lombok.Getter;
import scrumpledpaper.agiler.user.entity.User;

@Getter
@Builder
public class UserDto {
	private Long id;
	private String vendor;
	private String vendorId;
	private String email;
	private String nickname;
	private Long imageId;

	public static UserDto from(User user) {
		return UserDto.builder()
			.id(user.getId())
			.vendor(user.getVendor())
			.vendorId(user.getVendorId())
			.email(user.getEmail())
			.nickname(user.getNickname())
			.imageId(user.getImageId())
			.build();
	}
}
