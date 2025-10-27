package scrumpledpaper.agiler.project.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import scrumpledpaper.agiler.project.dto.ProjectCreateReqDto;
import scrumpledpaper.agiler.project.dto.ProjectCreateResDto;
import scrumpledpaper.agiler.project.dto.ProjectInfoResDto;
import scrumpledpaper.agiler.project.entity.Project;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
	Project toEntity(ProjectCreateReqDto projectCreateReqDto);
	ProjectCreateResDto toDto(Project savedProject);

	ProjectInfoResDto toProjectInfoResDto(Project project);

	List<ProjectInfoResDto> toProjectInfoResDtoList(List<Project> projects);
}
