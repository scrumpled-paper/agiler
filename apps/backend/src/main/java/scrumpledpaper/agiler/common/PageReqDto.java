package scrumpledpaper.agiler.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
	@Max(value = 100, message = "페이지 크기는 100 이하이어야 합니다.")
	private int size = 10;

	@Schema(defaultValue = "createdAt", allowableValues = {"createdAt"})
	@NotBlank(message = "정렬 기준은 필수입니다.")
	@Pattern(regexp = "^(createdAt)$", message = "유효하지 않은 정렬 기준입니다.")
	private String sort = "createdAt";

	@Schema(defaultValue = "DESC", allowableValues = {"ASC", "DESC"})
	@NotNull(message = "정렬 방향은 필수입니다.")
	@Pattern(regexp = "^(ASC|DESC)$", message = "정렬 방향은 ASC 또는 DESC여야 합니다.")
	private String direction = "DESC";

	public Pageable toPageable() {
		Sort.Direction dir = Sort.Direction.fromString(direction);
		return PageRequest.of(page, size, Sort.by(dir, sort));
	}
}
