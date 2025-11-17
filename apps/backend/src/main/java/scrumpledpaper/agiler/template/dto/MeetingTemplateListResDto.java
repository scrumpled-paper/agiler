package scrumpledpaper.agiler.template.dto;

import java.util.List;

public record MeetingTemplateListResDto(
	List<MeetingTemplateResDto> meetingTemplates,
	int size
) {}

