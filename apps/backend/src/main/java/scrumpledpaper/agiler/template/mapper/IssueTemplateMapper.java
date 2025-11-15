package scrumpledpaper.agiler.template.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.template.dto.IssueTemplateCreateReqDto;
import scrumpledpaper.agiler.template.entity.DefaultIssueTemplate;
import scrumpledpaper.agiler.template.entity.IssueTemplate;

@Mapper(componentModel = "spring")
public interface IssueTemplateMapper {
	@Mapping(target = "id", ignore = true)
	IssueTemplate toEntity(Project project, IssueTemplateCreateReqDto issueTemplateCreateReqDto);
}
