package scrumpledpaper.agiler.kanban.dto;

import java.util.List;

public record IssueAssigneesReqDto (
	List<Long> assignees
) {}
