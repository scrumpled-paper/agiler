package scrumpledpaper.agiler.template.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DefaultMeetingTemplate {
	TEAM_MEETING(
		"👥 팀 회의",
		"팀 회의 템플릿입니다.",
		"""
		## 회의 안건
		1.\s

		
		## 회의 결과
		-\s

		
		## 액션 아이템
		- [ ]\s
		"""
	);
	private final String title;
	private final String description;
	private final String contents;
}

