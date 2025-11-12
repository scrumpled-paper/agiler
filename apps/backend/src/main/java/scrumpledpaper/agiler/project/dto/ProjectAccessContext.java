package scrumpledpaper.agiler.project.dto;

import scrumpledpaper.agiler.project.entity.Profile;
import scrumpledpaper.agiler.project.entity.Project;

public record ProjectAccessContext(
	Project project,
	Profile profile
) {}

