package scrumpledpaper.agiler.project.entity;

import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;

public enum Role {
	MEMBER,
	OWNER;

	public static Role from(String value) {
		for (Role role : values()) {
			if (role.name().equalsIgnoreCase(value)) {
				return role;
			}
		}
		throw new CustomException(ErrorCode.INVALID_ROLE);
	}
}
