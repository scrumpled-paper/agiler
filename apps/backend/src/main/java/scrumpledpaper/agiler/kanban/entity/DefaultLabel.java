package scrumpledpaper.agiler.kanban.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DefaultLabel {
	FEATURE("feature", "#a2eeef", "New feature or request"),
	BUG("bug", "#d73a4a", "Something isn't working"),
	FIX("fix", "#0e8a16", "A code change that fixes an issue"),
	DOCUMENTATION("documentation", "#0075ca", "Improvements or additions to documentation");

	private final String name;
	private final String color;
	private final String description;
}
