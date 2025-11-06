package scrumpledpaper.agiler.common;

import org.springframework.data.domain.Page;

import lombok.experimental.UtilityClass;
import scrumpledpaper.agiler.common.exception.CustomException;
import scrumpledpaper.agiler.common.exception.ErrorCode;

@UtilityClass
public class PageValidator {
	public static <T> void validatePageInRange(Page<T> page) {
		if (page.isEmpty() && page.getTotalElements() > 0) {
			throw new CustomException(ErrorCode.PAGE_NOT_FOUND);
		}
	}
}
