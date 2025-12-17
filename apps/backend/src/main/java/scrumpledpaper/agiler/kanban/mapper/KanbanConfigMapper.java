package scrumpledpaper.agiler.kanban.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import scrumpledpaper.agiler.kanban.dto.KanbanConfigResDto;
import scrumpledpaper.agiler.kanban.dto.KanbanConfigUpdateReqDto;
import scrumpledpaper.agiler.kanban.entity.DefaultKanbanConfig;
import scrumpledpaper.agiler.kanban.entity.KanbanConfig;
import scrumpledpaper.agiler.project.entity.Project;

@Mapper(componentModel = "spring")
public interface KanbanConfigMapper {
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "project", source = "project")
	@Mapping(target = "version", source = "version")
	KanbanConfig toEntity(Project project, KanbanConfigUpdateReqDto.KanbanConfigReqDto dto, int version);

	List<KanbanConfigResDto> toDtoList(List<KanbanConfig> kanbanConfigs);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "project", source = "savedProject")
	@Mapping(target = "isDone", source = "defaultKanbanConfig.done")
	KanbanConfig toEntity(Project savedProject, DefaultKanbanConfig defaultKanbanConfig, int version);
}
