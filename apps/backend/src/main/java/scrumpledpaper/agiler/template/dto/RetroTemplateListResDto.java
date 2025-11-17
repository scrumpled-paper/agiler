package scrumpledpaper.agiler.template.dto;

import java.util.List;

public record RetroTemplateListResDto(
	List<RetroTemplateResDto> retroTemplates,
	int size
) {}

