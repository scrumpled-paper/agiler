package scrumpledpaper.agiler.template.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.template.dto.IssueTemplateCreateReqDto;
import scrumpledpaper.agiler.template.dto.IssueTemplateDetailResDto;
import scrumpledpaper.agiler.template.dto.IssueTemplateResDto;
import scrumpledpaper.agiler.template.entity.DefaultIssueTemplate;
import scrumpledpaper.agiler.template.entity.IssueTemplate;

@Mapper(componentModel = "spring")
public interface IssueTemplateMapper {
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "title", source = "defaultIssueTemplate.title")
	@Mapping(target = "description", source = "defaultIssueTemplate.description")
	@Mapping(target = "contents", source = "defaultIssueTemplate.contents")
	IssueTemplate toEntity(Project project, DefaultIssueTemplate defaultIssueTemplate);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "title", source = "issueTemplateCreateReqDto.title")
	@Mapping(target = "description", source = "issueTemplateCreateReqDto.description")
	@Mapping(target = "contents", source = "issueTemplateCreateReqDto.contents")
	IssueTemplate toEntity(Project project, IssueTemplateCreateReqDto issueTemplateCreateReqDto);

	@Mapping(target = "templateId", source = "id")
	IssueTemplateResDto toDto(IssueTemplate issueTemplate);

	IssueTemplateDetailResDto toDetailDto(IssueTemplate issueTemplate);
}
