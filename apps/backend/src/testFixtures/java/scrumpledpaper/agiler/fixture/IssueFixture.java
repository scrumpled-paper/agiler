package scrumpledpaper.agiler.fixture;

import java.time.LocalDateTime;
import java.util.List;

import scrumpledpaper.agiler.kanban.dto.IssueCreateReqDto;

public class IssueFixture {
	public static IssueCreateReqDto createIssueCreateReqDto(
		String title,
		String contents,
		Long assigneeId,
		List<Long> labels,
		LocalDateTime startedAt,
		LocalDateTime dueAt
	) {
		return new IssueCreateReqDto(
			title,
			contents,
			assigneeId,
			labels,
			startedAt,
			dueAt
		);
	}
}
