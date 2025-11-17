package scrumpledpaper.agiler.template.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.template.entity.DefaultRetroTemplate;

@Mapper(componentModel = "spring")
public interface RetroTemplateMapper {
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "title", source = "defaultRetroTemplate.title")
	@Mapping(target = "description", source = "defaultRetroTemplate.description")
	@Mapping(target = "contents", source = "defaultRetroTemplate.contents")
	RetroTemplate toEntity(Project project, DefaultRetroTemplate defaultRetroTemplate);

}

