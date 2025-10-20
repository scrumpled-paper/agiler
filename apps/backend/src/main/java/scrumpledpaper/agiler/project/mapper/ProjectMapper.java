package scrumpledpaper.agiler.project.mapper;

import org.mapstruct.Mapper;

import scrumpledpaper.agiler.project.dto.ProjectCreateReqDto;
import scrumpledpaper.agiler.project.dto.ProjectCreateResDto;
import scrumpledpaper.agiler.project.entity.Project;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
	Project toEntity(ProjectCreateReqDto projectCreateReqDto);
	ProjectCreateResDto toDto(Project savedProject);
}
