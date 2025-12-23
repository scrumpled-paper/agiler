import { apiClient } from '../client'
import {
  type MeetingListResponse,
  type RetroListResponse,
  type ScrumListResponse,
  type CreateActivityPayload,
  type DeleteActivityPayload,
  type ActivityCreateResponse,
  type PageRequestParams,
  type ActivityItem,
  type RawActivityItem,
} from '@/types/activity' // 해당 타입들은 프로젝트 규격에 맞게 정의되어 있다고 가정합니다.

// 리소스 경로와 응답 타입을 매핑
const RESOURCE_MAP = {
  meetings: { path: 'meetings', listType: {} as MeetingListResponse },
  retros: { path: 'retros', listType: {} as RetroListResponse },
  scrums: { path: 'scrums', listType: {} as ScrumListResponse },
} as const

type ResourceType = keyof typeof RESOURCE_MAP

/**
 * 프로젝트 내의 활동(Meetings, Retros, Scrums)을 관리하는 통합 서비스입니다.
 */
export const projectActivityService = {
  baseURL: '/api/v1/projects',

  /**
   * 활동 목록 조회 (GET)
   */
  async getActivities<T extends ResourceType>(
    projectUrl: string | undefined,
    resourceType: T,
    params: PageRequestParams
  ): Promise<(typeof RESOURCE_MAP)[T]['listType']> {
    if (!projectUrl) throw new Error('프로젝트 URL이 필요합니다.')

    const resourcePath = RESOURCE_MAP[resourceType].path
    const response = await apiClient.get(
      `${this.baseURL}/${projectUrl}/${resourcePath}`,
      { params }
    )
    // return response.data
    // [ ] api 수정 후 복구
    // --- 매핑 로직 시작 ---
    const rawData = response.data

    return {
      ...rawData,
      contents: rawData.contents.map((item: RawActivityItem): ActivityItem => {
        // 우선순위에 따라 ID 추출 (nullish coalescing 사용)
        const activityId =
          item.id ?? item.meetingId ?? item.retroId ?? item.scrumId

        if (activityId === undefined) {
          console.warn('ID 필드를 찾을 수 없습니다:', item)
        }

        return {
          id: activityId ?? 0, // ID가 없을 경우 대비 (fallback)
          title: item.title,
          createdAt: item.createdAt,
          participants: item.participants,
        }
      }),
    }
  },

  /**
   * 새로운 활동 생성 (POST)
   */
  async createActivity(
    projectUrl: string,
    resourceType: ResourceType,
    payload: CreateActivityPayload
  ): Promise<ActivityCreateResponse> {
    const resourcePath = RESOURCE_MAP[resourceType].path
    const response = await apiClient.post(
      `${this.baseURL}/${projectUrl}/${resourcePath}`,
      payload
    )
    return response.data
  },

  /**
   * 활동 삭제 (DELETE)
   */
  async deleteActivity(
    projectUrl: string,
    resourceType: ResourceType,
    payload: DeleteActivityPayload
  ): Promise<void> {
    const resourcePath = RESOURCE_MAP[resourceType].path
    await apiClient.delete(`${this.baseURL}/${projectUrl}/${resourcePath}`, {
      data: payload,
    })
  },
}

// --- 각 도메인별 명시적 서비스 객체 ---

export const meetingService = {
  async getMeetings(projectUrl: string, params: PageRequestParams) {
    return projectActivityService.getActivities(projectUrl, 'meetings', params)
  },
  async createMeeting(projectUrl: string, templateId: number) {
    return projectActivityService.createActivity(projectUrl, 'meetings', {
      templateId,
    })
  },
  async deleteMeeting(projectUrl: string, id: number) {
    return projectActivityService.deleteActivity(projectUrl, 'meetings', { id })
  },
}

export const retroService = {
  async getRetros(projectUrl: string, params: PageRequestParams) {
    return projectActivityService.getActivities(projectUrl, 'retros', params)
  },
  async createRetro(projectUrl: string, templateId: number) {
    return projectActivityService.createActivity(projectUrl, 'retros', {
      templateId,
    })
  },
  async deleteRetro(projectUrl: string, id: number) {
    return projectActivityService.deleteActivity(projectUrl, 'retros', { id })
  },
}

export const scrumService = {
  async getScrums(projectUrl: string, params: PageRequestParams) {
    return projectActivityService.getActivities(projectUrl, 'scrums', params)
  },
  async createScrum(projectUrl: string, templateId: number) {
    return projectActivityService.createActivity(projectUrl, 'scrums', {
      templateId,
    })
  },
  async deleteScrum(projectUrl: string, id: number) {
    return projectActivityService.deleteActivity(projectUrl, 'scrums', { id })
  },
}
