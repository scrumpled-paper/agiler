package scrumpledpaper.agiler.template.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.template.dto.ScrumTemplateCreateReqDto;
import scrumpledpaper.agiler.template.dto.ScrumTemplateDetailResDto;
import scrumpledpaper.agiler.template.dto.ScrumTemplateResDto;
import scrumpledpaper.agiler.template.entity.DefaultScrumTemplate;
import scrumpledpaper.agiler.template.entity.ScrumTemplate;

@Mapper(componentModel = "spring")
public interface ScrumTemplateMapper {
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "title", source = "defaultScrumTemplate.title")
	@Mapping(target = "description", source = "defaultScrumTemplate.description")
	@Mapping(target = "contents", source = "defaultScrumTemplate.contents")
	ScrumTemplate toEntity(Project project, DefaultScrumTemplate defaultScrumTemplate);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "title", source = "scrumTemplateCreateReqDto.title")
	@Mapping(target = "description", source = "scrumTemplateCreateReqDto.description")
	@Mapping(target = "contents", source = "scrumTemplateCreateReqDto.contents")
	ScrumTemplate toEntity(Project project, ScrumTemplateCreateReqDto scrumTemplateCreateReqDto);

	@Mapping(target = "templateId", source = "id")
	ScrumTemplateResDto toDto(ScrumTemplate scrumTemplate);

	ScrumTemplateDetailResDto toDetailDto(ScrumTemplate scrumTemplate);
}
