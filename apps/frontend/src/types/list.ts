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
// T는 ListContentItem 인터페이스를 상속받거나, 적어도 그와 유사한 구조를 가질 것으로 가정합니다.
export interface PagedResponse<T extends ListContentItem> {
  contents: T[]
  size: number
  number: number
  totalPages: number
}
