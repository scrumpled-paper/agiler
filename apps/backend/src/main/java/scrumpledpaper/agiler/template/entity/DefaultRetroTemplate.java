package scrumpledpaper.agiler.template.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DefaultRetroTemplate {
	SPRINT_RETROSPECTIVE(
		"🔄 스프린트 회고",
		"스프린트 회고 템플릿입니다.",
		"""
		## 잘한 점 (Keep)
		-\s

		
		## 개선할 점 (Improve)
		-\s

		
		## 도전할 점 (Try)
		-\s
		"""
	);
	private final String title;
	private final String description;
	private final String contents;
}

