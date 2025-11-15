package scrumpledpaper.agiler.template.dto;

import java.util.List;

public record IssueTemplateListResDto(
	List<IssueTemplateResDto> issueTemplates,
	int size
) {}
