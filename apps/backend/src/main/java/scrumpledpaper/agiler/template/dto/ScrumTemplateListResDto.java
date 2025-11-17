package scrumpledpaper.agiler.template.dto;

import java.util.List;

public record ScrumTemplateListResDto(
	List<ScrumTemplateResDto> scrumTemplates,
	int size
) {}

