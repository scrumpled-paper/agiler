// 참여자 정보를 나타내는 인터페이스
export interface Participant {
  id: number
  nickname: string
  imageUrl: string
}

// 데일리스크럼, 회고, 회의록 리스트 항목의 공통 속성
export interface ListContentItem {
  id: number
  title: string
  createdAt: Date | string // datetime은 Date 객체 혹은 string으로 가정
  participants: Participant[]
}

// 제네릭 T를 사용하여 어떤 타입의 리스트 아이템이든 포함할 수 있는 공통 응답 인터페이스
// T는 최소한 id 속성을 가져야 합니다 (테이블 렌더링의 key로 사용)
export interface PagedResponse<T = unknown> {
  contents: T[]
  size: number
  number: number
  totalPages: number
}
