import {
  type LabelUpdateParams,
  type LabelCreateParams,
  type LabelDeleteParams,
  type LabelListResponse,
} from '@/types/label'
import { apiClient } from '../client'

type TempLabel = {
  id: number
  name: string
  description: string
  color: string // 예: "#4bB68C"
}

export const labelService = {
  baseURL: '/api/v1/projects',

  // 특정 프로젝트의 모든 레이블 목록을 불러옵니다.

  async getLabels(projectUrl: string): Promise<LabelListResponse> {
    const response = await apiClient.get(`${this.baseURL}/${projectUrl}/labels`)
    const mappedLabels = response.data.labels.map((label: TempLabel) => {
      return {
        labelId: label.id,
        name: label.name,
        description: label.description,
        color: label.color,
      }
    })

    return {
      labels: mappedLabels,
      size: response.data.size,
    }
  },

  //  * 새 레이블을 생성합니다.
  async createLabel(
    projectUrl: string,
    payload: LabelCreateParams
  ): Promise<void> {
    await apiClient.post(`${this.baseURL}/${projectUrl}/labels`, payload)
  },

  //  * 기존 레이블의 정보를 수정합니다.
  async updateLabel(
    projectUrl: string,
    labelId: number,
    payload: LabelUpdateParams
  ): Promise<void> {
    await apiClient.put(
      `${this.baseURL}/${projectUrl}/labels/${labelId}`,
      payload
    )
  },

  //  * 특정 레이블을 삭제합니다.
  async deleteLabel(
    projectUrl: string,
    payload: LabelDeleteParams
  ): Promise<void> {
    await apiClient.delete(`${this.baseURL}/${projectUrl}/labels`, {
      data: payload,
    })
  },
}
