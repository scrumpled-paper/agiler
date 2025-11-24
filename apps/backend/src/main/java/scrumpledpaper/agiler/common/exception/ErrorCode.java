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
	JSON_PROCESSING_ERROR(500, "C003", "JSON 처리 중 오류가 발생했습니다."),
	REDIS_NOT_FOUND_STATE(404, "C004", "Redis에서 상태를 찾을 수 없습니다."),

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
	RETRO_TEMPLATE_NOT_FOUND(404, "T003", "회고 템플릿을 찾을 수 없습니다."),
	MEETING_TEMPLATE_NOT_FOUND(404, "T004", "회의 템플릿을 찾을 수 없습니다."),

	DEFAULT_KANBAN_CONFIG_NOT_FOUND(404, "K001", "Default 칸반 설정을 찾을 수 없습니다."),
	KANBAN_CONFIG_NOT_FOUND(404, "K002", "칸반 설정을 찾을 수 없습니다."),

	ISSUE_NOT_FOUND(404, "IS001", "이슈를 찾을 수 없습니다."),

	NOTIFICATION_CHANNEL_NOT_FOUND(404, "N001", "알림 채널을 찾을 수 없습니다."),
	SLACK_OAUTH_FAILED(400, "N002", "Slack OAuth 인증에 실패했습니다."),
	DISCORD_OAUTH_FAILED(400, "N003", "Discord OAuth 인증에 실패했습니다."),
	CHANNEL_WEBHOOK_ERROR(500, "N004", "채널 웹훅 처리 중 오류가 발생했습니다."),
	NOTIFICATION_UNAUTHORIZED(403, "N005", "알림 채널에 대한 권한이 없습니다."),
	NOTIFICATION_SUBSCRIPTION_NOT_FOUND(404, "N006", "알림 구독을 찾을 수 없습니다."),
	INVALID_SCHEDULE_REQUEST(400, "N007", "잘못된 알림 예약 요청입니다. delayInMinutes 또는 notificationTime 중 하나만 지정해야 합니다."),
	DUPLICATE_NOTIFICATION_CHANNEL(409, "N008", "이미 등록된 알림 채널입니다.");

	private final int status;
	private final String code;
	private final String message;

	ErrorCode(int status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}
}
