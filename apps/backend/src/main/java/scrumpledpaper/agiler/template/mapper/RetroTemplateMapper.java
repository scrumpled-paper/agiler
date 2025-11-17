package scrumpledpaper.agiler.template.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.template.dto.RetroTemplateCreateReqDto;
import scrumpledpaper.agiler.template.dto.RetroTemplateDetailResDto;
import scrumpledpaper.agiler.template.dto.RetroTemplateResDto;
import scrumpledpaper.agiler.template.entity.DefaultRetroTemplate;
import scrumpledpaper.agiler.template.entity.RetroTemplate;

@Mapper(componentModel = "spring")
public interface RetroTemplateMapper {
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "title", source = "defaultRetroTemplate.title")
	@Mapping(target = "description", source = "defaultRetroTemplate.description")
	@Mapping(target = "contents", source = "defaultRetroTemplate.contents")
	RetroTemplate toEntity(Project project, DefaultRetroTemplate defaultRetroTemplate);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "title", source = "retroTemplateCreateReqDto.title")
	@Mapping(target = "description", source = "retroTemplateCreateReqDto.description")
	@Mapping(target = "contents", source = "retroTemplateCreateReqDto.contents")
	RetroTemplate toEntity(Project project, RetroTemplateCreateReqDto retroTemplateCreateReqDto);

	@Mapping(target = "templateId", source = "id")
	RetroTemplateResDto toDto(RetroTemplate retroTemplate);

	RetroTemplateDetailResDto toDetailDto(RetroTemplate retroTemplate);
}

