package scrumpledpaper.agiler.template.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.template.entity.DefaultMeetingTemplate;
import scrumpledpaper.agiler.template.entity.MeetingTemplate;

@Mapper(componentModel = "spring")
public interface MeetingTemplateMapper {
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "title", source = "defaultMeetingTemplate.title")
	@Mapping(target = "description", source = "defaultMeetingTemplate.description")
	@Mapping(target = "contents", source = "defaultMeetingTemplate.contents")
	MeetingTemplate toEntity(Project project, DefaultMeetingTemplate defaultMeetingTemplate);

}

