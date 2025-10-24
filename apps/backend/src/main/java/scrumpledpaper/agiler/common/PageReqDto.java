package scrumpledpaper.agiler.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageReqDto {

	@Schema(defaultValue = "0")
	@Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
	private int page = 0;

	@Schema(defaultValue = "10")
	@Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
	private int size = 10;

	@Schema(defaultValue = "createdAt")
	private String sort = "createdAt";

	@Schema(defaultValue = "DESC", allowableValues = {"ASC", "DESC"})
	private String direction = "DESC";

	public Pageable toPageable() {
		if (sort == null || sort.isEmpty()) {
			return PageRequest.of(page, size);
		}

		Sort.Direction dir;
		try {
			dir = Sort.Direction.fromString(direction != null ? direction : "DESC");
		} catch (IllegalArgumentException e) {
			dir = Sort.Direction.DESC;
		}

		return PageRequest.of(page, size, Sort.by(dir, sort));
	}
}
