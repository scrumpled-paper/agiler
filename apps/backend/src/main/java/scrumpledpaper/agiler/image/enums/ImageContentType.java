package scrumpledpaper.agiler.image.enums;

import lombok.Getter;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;

@Getter
public enum ImageContentType {

	IMAGE_JPEG("image/jpeg"),
	IMAGE_PNG("image/png"),
	IMAGE_JPG("image/jpg"),
	IMAGE_GIF("image/gif");

	private final String mimeType;

	ImageContentType(String mimeType) {
		this.mimeType = mimeType;
	}

	public static ImageContentType from(String contentType) {
		for (ImageContentType type : values()) {
			if (type.getMimeType().equalsIgnoreCase(contentType)) {
				return type;
			}
		}

		throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
	}

}
