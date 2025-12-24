/**
 * 공통 페이징 요청 파라미터
 */
export interface PageRequestParams {
  page?: number
  size?: number
  sort?: string[]
  // 스웨거의 pageReqDto 구조에 따라 추가 필드를 정의하세요.
}

/**
 * 공통 페이징 응답 구조
 */
export interface PaginatedResponse<T> {
  pageSize: number
  currentPage: number
  totalPages: number
  totalElements: number
  contents: T[]
}

/**
 * 참여자 정보
 */
export interface Participant {
  profileId: number
  nickname: string
  imageUrl: string
}

// 서버에서 내려주는 원시 데이터 형태
export interface RawActivityItem {
  meetingId?: number
  retroId?: number
  scrumId?: number
  id?: number
  title: string
  createdAt: string
  participants: Participant[]
}
/**
 * 모든 활동(Meeting, Retro, Scrum)에 공통으로 사용되는 아이템 구조
 * ID 필드가 'id'로 통일된 경우입니다.
 */
export interface ActivityItem {
  id: number // meetingId, retroId, scrumId 대신 공통 id 사용
  title: string
  createdAt: string
  participants: Participant[]
}

/**
 * 서비스별 응답 타입
 * 이제 모두 동일한 ActivityItem 구조를 사용하므로 관리가 매우 편리합니다.
 */
export type MeetingListResponse = PaginatedResponse<ActivityItem>
export type RetroListResponse = PaginatedResponse<ActivityItem>
export type ScrumListResponse = PaginatedResponse<ActivityItem>

/**
 * 생성/삭제 관련 타입
 */
export interface CreateActivityPayload {
  templateId: number
}

export interface DeleteActivityPayload {
  id: number
}

export interface ActivityCreateResponse {
  id: number
}
