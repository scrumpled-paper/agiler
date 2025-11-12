export interface ContentItem {
  title: string
  url: string
  imageUrl: string
  summary: string
}

export interface PaginatedContentResponse {
  contents: ContentItem[]
  pageSize: number
  currentPage: number
  totalPages: number
  totalItems: number
}

export interface UserInfo {
  nickname: string
  email?: string
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
