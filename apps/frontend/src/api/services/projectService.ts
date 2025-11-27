import type {
  GetProjectListParams,
  GetProjectListResponse,
  GetProjectMembersResponse,
  GetProjectMembersParams,
  ProjectInfo,
  GetProjectSummaryResponse,
  UserInfo,
  UserUpdateParams,
} from '@/types'
import { apiClient } from '../client'

export const projectService = {
  // 공통 기본 URL을 속성으로 정의하여 중앙 관리
  apiUrl: `/api/v1/projects`,

  // 프로젝트 카드 목록 조회 (메인 페이지용)
  async getProjectList({
    size,
    page,
  }: GetProjectListParams): Promise<GetProjectListResponse> {
    const listInfoUrl = `${this.apiUrl}/info`
    const response = await apiClient.get<GetProjectListResponse>(listInfoUrl, {
      params: { size, page },
    })
    return response.data
  },

  // 사이드바용 프로젝트 목록 조회
  async getProjectSidebar({
    size,
    page,
  }: GetProjectListParams): Promise<GetProjectListResponse> {
    const listUrl = this.apiUrl
    const response = await apiClient.get<GetProjectListResponse>(listUrl, {
      params: { size, page },
    })
    return response.data
  },

  // 프로젝트 상세내용 조회
  async getProjectSummery(
    projectUrl: string
  ): Promise<GetProjectSummaryResponse> {
    const infoUrl = `${this.apiUrl}/${projectUrl}`
    const response = await apiClient.get<GetProjectSummaryResponse>(infoUrl)
    return response.data
  },
  //프로젝트 상세내용 수정
  async updateProjectSummery(
    projectUrl: string,
    { title, url, summary }: ProjectInfo
  ): Promise<number> {
    const infoUrl = `${this.apiUrl}/${projectUrl}`
    const response = await apiClient.put<number>(infoUrl, {
      title,
      url,
      summary,
    })
    return response.data
  },

  // 프로젝트 생성 URL 검증

  async getProjectUrlCheck(projectUrl: string): Promise<boolean> {
    const checkUrl = `${this.apiUrl}/check` // 경로에는 projectUrl이 없음

    // Axios 기준으로 { params: { url: projectUrl } } 객체를 전달하여 쿼리 파라미터를 만듦
    const response = await apiClient.get(checkUrl, {
      params: {
        url: projectUrl,
      },
    })

    return response.data
  },
  //  프로젝트 생성
  async createProject({ title, url, summary }: ProjectInfo): Promise<number> {
    const createUrl = this.apiUrl
    const response = await apiClient.post(createUrl, {
      title,
      url,
      summary,
    })
    return response.data
  },

  // 프로젝트 멤버 조회
  async getProjectMember({
    projectUrl,
    size,
    page,
  }: GetProjectMembersParams): Promise<GetProjectMembersResponse> {
    const memberUrl = `${this.apiUrl}/${projectUrl}/profiles`
    const response = await apiClient.get<GetProjectMembersResponse>(memberUrl, {
      params: {
        size,
        page,
      },
    })
    return response.data
  },
  async getUserInfo(projectUrl: string): Promise<UserInfo> {
    const userUrl = `${this.apiUrl}/${projectUrl}/profiles/me`
    const response = await apiClient.get(userUrl)
    return response.data
  },
  async getMemberProfileById(
    projectUrl: string,
    profileId: number
  ): Promise<UserInfo> {
    const memberUrl = `${this.apiUrl}/${projectUrl}/profiles/${profileId}`

    const response = await apiClient.get<UserInfo>(memberUrl)

    return response.data
  },

  async updateMyProfile(
    projectUrl: string,
    payload: UserUpdateParams
  ): Promise<void> {
    const updateUrl = `${this.apiUrl}/${projectUrl}/profiles`
    // 응답이 200 OK이고 본문이 비어있으므로 반환 타입은 void로 설정
    await apiClient.put(updateUrl, payload)
  },
  async updateMemberRole(
    projectUrl: string,
    payload: UserUpdateParams
  ): Promise<void> {
    const roleUrl = `${this.apiUrl}/${projectUrl}/profiles/role`
    // 응답이 200 OK이고 본문이 비어있으므로 반환 타입은 void로 설정
    await apiClient.patch(roleUrl, payload)
  },
}
