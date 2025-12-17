package scrumpledpaper.agiler.kanban.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import scrumpledpaper.agiler.kanban.dto.KanbanBoardResDto;
import scrumpledpaper.agiler.kanban.dto.LabelCreateReqDto;
import scrumpledpaper.agiler.kanban.dto.LabelResDto;
import scrumpledpaper.agiler.kanban.entity.DefaultLabel;
import scrumpledpaper.agiler.kanban.entity.Label;
import scrumpledpaper.agiler.project.entity.Project;

@Mapper(componentModel = "spring")
public interface LabelMapper {
	@Mapping(target = "id", ignore = true)
	Label toEntity(Project project, LabelCreateReqDto labelCreateReqDto);

	@Mapping(target = "id", ignore = true)
	Label toEntity(Project project, DefaultLabel defaultLabel);

	LabelResDto toDto(Label label);

	@Mapping(target = "labelId", source = "label.id")
	KanbanBoardResDto.LabelDto toKanbanDto(Label label);
}
