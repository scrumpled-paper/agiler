package scrumpledpaper.agiler.common;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResDto<T> {
	private int pageSize;
	private int currentPage;
	private int totalPages;
	private long totalElements;
	private List<T> contents;

	public static <T> PageResDto<T> from(Page<T> page) {
		return PageResDto.<T>builder()
			.pageSize(page.getSize())
			.currentPage(page.getNumber())
			.totalPages(page.getTotalPages())
			.totalElements(page.getTotalElements())
			.contents(page.getContent())
			.build();
	}

	public static <T> PageResDto<T> empty(Page<?> page) {
		return PageResDto.<T>builder()
			.pageSize(page.getSize())
			.currentPage(page.getNumber())
			.totalPages(0)
			.totalElements(0L)
			.contents(Collections.emptyList())
			.build();
	}
}
