package scrumpledpaper.agiler.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
	INVALID_TOKEN(401, "A001", "유효하지 않은 토큰입니다."),
	EXPIRED_TOKEN(403, "A002", "만료된 토큰입니다."),

	USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다."),

	IMAGE_NOT_FOUND(404, "I001", "이미지를 찾을 수 없습니다."),

	INVALID_REQUEST(400, "C001", "잘못된 요청입니다."),

	PROJECT_URL_ALREADY_EXISTS(409, "P001", "이미 존재하는 프로젝트 URL입니다.");

	private final int status;
	private final String code;
	private final String message;

	ErrorCode(int status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}
}
