package scrumpledpaper.agiler.kanban.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import scrumpledpaper.agiler.kanban.dto.LabelCreateReqDto;
import scrumpledpaper.agiler.kanban.entity.DefaultLabel;
import scrumpledpaper.agiler.kanban.entity.Label;
import scrumpledpaper.agiler.project.entity.Project;

@Mapper(componentModel = "spring")
public interface LabelMapper {
	@Mapping(target = "id", ignore = true)
	Label toEntity(Project project, LabelCreateReqDto labelCreateReqDto);

	@Mapping(target = "id", ignore = true)
	Label toEntity(Project project, DefaultLabel defaultLabel);
}
