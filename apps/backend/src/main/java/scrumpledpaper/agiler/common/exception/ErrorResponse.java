package scrumpledpaper.agiler.common.exception;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
class ErrorResponse {
	private int status;
	private final String code;
	private final String message;
	private Map<String, String> fieldErrors;

	public ErrorResponse(ErrorCode errorCode) {
		this.status = errorCode.getStatus();
		this.code = errorCode.getCode();
		this.message = errorCode.getMessage();
	}

	public ErrorResponse(ErrorCode errorCode, Map<String, String> fieldErrors) {
		this(errorCode);
		this.fieldErrors = fieldErrors;
	}
}
