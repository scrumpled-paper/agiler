package scrumpledpaper.agiler.kanban.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import scrumpledpaper.agiler.kanban.dto.KanbanConfigUpdateReqDto;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.project.entity.Project;

@Mapper(componentModel = "spring")
public interface KanbanConfigMapper {
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "project", source = "project")
	KanbanConfig toEntity(Project project, KanbanConfigUpdateReqDto.KanbanConfigReqDto dto);
}
