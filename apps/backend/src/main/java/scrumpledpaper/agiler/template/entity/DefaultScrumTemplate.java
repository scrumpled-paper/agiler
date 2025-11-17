package scrumpledpaper.agiler.template.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DefaultScrumTemplate {
	TODAYS_SCRUM(
		"📋 N/N 스크럼 회의",
		"오늘 진행할 스크럼 회의 템플릿입니다.",
		"""
		## 어제 한 일
		-\s

		
		## 오늘 할 일
		-\s

		
		## 공유사항
		-\s
		"""
	);

	private final String title;
	private final String description;
	private final String contents;
}
