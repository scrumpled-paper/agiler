package scrumpledpaper.agiler.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
	INVALID_TOKEN(401, "A001", "유효하지 않은 토큰입니다."),
	EXPIRED_TOKEN(403, "A002", "만료된 토큰입니다."),
	UNAUTHORIZED(401, "A003", "인증되지 않은 사용자입니다."),
	FORBIDDEN(403, "A004", "접근이 금지된 리소스입니다."),
	OAUTH2_PROCESSING_ERROR(400, "A005", "OAuth2 인증 후 처리 중 오류가 발생했습니다."),

	USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다."),

	IMAGE_NOT_FOUND(404, "I001", "이미지를 찾을 수 없습니다."),
	INVALID_CONTENT_TYPE(400, "I002", "유효하지 않은 콘텐츠 타입입니다."),

	INVALID_REQUEST(400, "C001", "잘못된 요청입니다."),
	PAGE_NOT_FOUND(404, "C002", "페이지를 찾을 수 없습니다."),

	PROJECT_URL_ALREADY_EXISTS(409, "P001", "이미 존재하는 프로젝트 URL입니다."),
	PROJECT_NOT_FOUND(404, "P002", "프로젝트를 찾을 수 없습니다."),
	PROJECT_NOT_MEMBER(403, "P003", "프로젝트 참여자가 아닙니다."),
	PROJECT_OWNER_REQUIRED(403, "P004", "이 작업은 프로젝트 오너 권한이 필요합니다"),
	PROJECT_PROFILE_NOT_FOUND(404, "P005", "프로젝트 참여자를 찾을 수 없습니다."),
	PROJECT_OWNER_MINIMUM_REQUIRED(400, "P006", "프로젝트 관리자는 최소 한 명 이상이어야 합니다."),
	INVALID_ROLE(400, "P007", "유효하지 않은 역할입니다."),

	LABEL_NOT_FOUND(404, "L001", "라벨을 찾을 수 없습니다."),

	ISSUE_TEMPLATE_NOT_FOUND(404, "T001", "이슈 템플릿을 찾을 수 없습니다."),
	SCRUM_TEMPLATE_NOT_FOUND(404, "T002", "스크럼 템플릿을 찾을 수 없습니다."),
	RETRO_TEMPLATE_NOT_FOUND(404, "T003", "회고 템플릿을 찾을 수 없습니다.");

	private final int status;
	private final String code;
	private final String message;

	ErrorCode(int status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}
}
