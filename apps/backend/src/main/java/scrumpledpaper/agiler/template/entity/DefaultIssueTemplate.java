package scrumpledpaper.agiler.template.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DefaultIssueTemplate {
	BUG_FIX(
		"🐛 버그 수정",
		"오늘 수정해야 할 버그",
		"""
		## 버그 내용
		
		
		## 예상 소요 시간
		⏱️ [30분 / 1시간 / 2시간 / 반나절 / 하루]
		
		## 관련 파일/모듈
		
		"""
	),

	FEATURE_IMPLEMENTATION(
		"✨ 기능 구현",
		"오늘 개발할 기능",
		"""
		## 구현할 기능
		
		
		## 체크리스트
		- [ ] API 작성
		- [ ] 비즈니스 로직 구현
		- [ ] 테스트 작성
		- [ ] 프론트엔드 연동
		
		## 예상 소요 시간
		⏱️ [1시간 / 2시간 / 반나절 / 하루]
		"""
	),

	CODE_REVIEW(
		"👀 코드 리뷰",
		"오늘 리뷰할 코드",
		"""
		## 리뷰 대상
		- PR 링크:\s
		- 담당자:\s
		
		## 체크 포인트
		- [ ] 기능 동작 확인
		- [ ] 코드 품질
		- [ ] 테스트 커버리지
		
		## 예상 소요 시간
		⏱️ [30분 / 1시간]
		"""
	),

	TESTING(
		"🧪 테스트 작성",
		"오늘 작성할 테스트",
		"""
		## 테스트 대상
		
		
		## 테스트 종류
		- [ ] 단위 테스트
		- [ ] 통합 테스트
		- [ ] E2E 테스트
		
		## 예상 소요 시간
		⏱️ [1시간 / 2시간 / 반나절]
		"""
	),

	REFACTORING(
		"🔨 리팩토링",
		"오늘 개선할 코드",
		"""
		## 리팩토링 대상
		
		
		## 개선 내용
		- [ ] 코드 정리
		- [ ] 성능 개선
		- [ ] 구조 개선
		
		## 예상 소요 시간
		⏱️ [1시간 / 2시간 / 반나절]
		"""
	),

	DOCUMENTATION(
		"📝 문서 작성",
		"오늘 작성할 문서",
		"""
		## 문서 종류
		- [ ] API 문서
		- [ ] README
		- [ ] 사용자 가이드
		- [ ] 회의록
		
		## 작성 내용
		
		
		## 예상 소요 시간
		⏱️ [30분 / 1시간 / 2시간]
		"""
	),

	MEETING(
		"🗓️ 회의",
		"오늘 참석할 회의",
		"""
		## 회의 정보
		- 시간:\s
		- 참석자:\s
		- 장소/링크:\s
		
		## 안건
		1.\s
		2.\s
		
		## 준비사항
		- [ ]\s
		
		## 소요 시간
		⏱️ [30분 / 1시간 / 2시간]
		"""
	);

	private final String title;
	private final String description;
	private final String contents;
}
