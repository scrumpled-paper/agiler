export interface UserInfo {
  profileId: number
  nickname: string
  email?: string
  imageUrl?: string
  role?: string
  description?: string
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
  contents: UserInfo[]
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

export interface GetProjectSummaryResponse {
  title: string
  summary: string
  imageUrl: string
}

// Task 관련 타입

export interface IssueColumn extends Record<string, unknown> {
  id: string
  name: string
  color?: string
}

// export interface Label extends Record<string, unknown> {
//   name: string
//   description: string
//   color: string // #FFFFFF 형태
// }

//  대시보드 유저 프로필 수정 (전역)
export interface UserUpdateParams {
  nickname: string
  email: string
}

//  프로젝트별 프로필 수정
export interface ProjectProfileUpdateParams {
  nickname: string
  email: string
  description: string
}

//  프로젝트 멤버 역할 수정
export interface MemberRoleUpdateParams {
  profileId: number
  role: string
}
