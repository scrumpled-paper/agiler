export interface UserInfo {
  nickname: string
  email?: string
  image?: string
}

export interface ProjectInfo {
  title: string
  url: string
  imageUrl?: string
  summary?: string
}
//프로젝트 리스트 API 전체 응답의 타입
export interface GetProjectListResponse {
  contents: ProjectInfo[]
  pageSize: number
  currentPage: number
  totalPages: number
  totalElements: number
}
// API 요청 시 필요한 파라미터 타입
export interface GetProjectListParams {
  size: number
  page: number
}

//프로젝트 사이드바 멤버
export interface ProjectMember {
  peopleId: number
  nickname: string
  email: string
  imageUrl: string
  role: string
  description: string
}

// 프로젝트 맴버 API 전체 응답의 타입
export interface GetProjectMembersResponse {
  contents: ProjectMember[]
  size: number
  number: number
  totalPages: number
}

// API 요청 시 필요한 파라미터 타입
export interface GetProjectMembersParams {
  projectUrl: string
  size: number
  page: number
}

// Task 관련 타입

export interface IssueColumn extends Record<string, unknown> {
  id: string
  name: string
  color?: string
}

export interface Issue extends Record<string, unknown> {
  id: string
  name: string
  startAt: Date
  endAt: Date
  column: string // TaskColumn의 id
  owner: UserInfo
  subscribers?: UserInfo[] // 알림 구독자 목록
  labels?: Label[]
}

export interface Label extends Record<string, unknown> {
  name: string
  description: string
  color: string // #FFFFFF 형태
}
