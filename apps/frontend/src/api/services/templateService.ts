// src/api/services/templateService.ts

import {
  type TemplateDetail,
  type TemplateCreatePayload,
  type TemplateUpdatePayload,
  type TemplateDeletePayload,
  // 각 리소스별 목록 응답 타입 (API 응답 스키마에 맞춰 정의 필요)
  type IssueTemplateListResponse,
  type MeetingTemplateListResponse,
  type RetroTemplateListResponse,
  type ScrumTemplateListResponse,
} from '@/types/template' // 통합 템플릿 타입과 각 목록 응답 타입은 여기서 임포트한다고 가정
import { apiClient } from '../client'

// 각 리소스의 경로와 목록 응답 타입을 매핑하는 객체
const RESOURCE_MAP = {
  issues: { path: 'issues', listType: {} as IssueTemplateListResponse },
  meetings: { path: 'meetings', listType: {} as MeetingTemplateListResponse },
  retros: { path: 'retros', listType: {} as RetroTemplateListResponse },
  scrums: { path: 'scrums', listType: {} as ScrumTemplateListResponse },
} as const

// 리소스 타입을 정의하여 타입 안정성을 확보
type ResourceType = keyof typeof RESOURCE_MAP

/**
 * 프로젝트 내의 모든 유형의 템플릿(Issue, Meeting, Retro, Scrum)을 관리하는 통합 서비스입니다.
 */
export const templateService = {
  baseURL: '/api/v1/projects',

  // --- 헬퍼 함수: CRUD 작업을 수행하는 범용 함수 ---

  /**
   * 템플릿 목록을 조회하는 범용 함수
   * @param projectUrl 프로젝트 URL
   * @param resourceType 'issues', 'meetings', 'retros', 'scrums' 중 하나
   */
  async getTemplates<T extends ResourceType>(
    projectUrl: string,
    resourceType: T
  ): Promise<(typeof RESOURCE_MAP)[T]['listType']> {
    const resourcePath = RESOURCE_MAP[resourceType].path
    const response = await apiClient.get(
      `${this.baseURL}/${projectUrl}/${resourcePath}/templates`
    )
    return response.data
  },

  /**
   * 템플릿 상세 정보를 조회하는 범용 함수
   * @param projectUrl 프로젝트 URL
   * @param resourceType 리소스 타입
   * @param templateId 템플릿 ID
   */
  async getTemplateDetail(
    projectUrl: string,
    resourceType: ResourceType,
    templateId: number
  ): Promise<TemplateDetail> {
    const resourcePath = RESOURCE_MAP[resourceType].path
    const response = await apiClient.get(
      `${this.baseURL}/${projectUrl}/${resourcePath}/templates/${templateId}`
    )
    return response.data
  },

  /**
   * 새 템플릿을 생성하는 범용 함수
   * @param projectUrl 프로젝트 URL
   * @param resourceType 리소스 타입
   * @param payload 생성할 템플릿 데이터
   */
  async createTemplate(
    projectUrl: string,
    resourceType: ResourceType,
    payload: TemplateCreatePayload
  ): Promise<void> {
    const resourcePath = RESOURCE_MAP[resourceType].path
    await apiClient.post(
      `${this.baseURL}/${projectUrl}/${resourcePath}/templates`,
      payload
    )
  },

  /**
   * 기존 템플릿을 수정하는 범용 함수
   * @param projectUrl 프로젝트 URL
   * @param resourceType 리소스 타입
   * @param payload 수정할 템플릿 데이터 (templateId 포함)
   */
  async updateTemplate(
    projectUrl: string,
    resourceType: ResourceType,
    payload: TemplateUpdatePayload
  ): Promise<void> {
    const resourcePath = RESOURCE_MAP[resourceType].path
    await apiClient.put(
      `${this.baseURL}/${projectUrl}/${resourcePath}/templates`,
      payload
    )
  },

  /**
   * 템플릿을 삭제하는 범용 함수
   * @param projectUrl 프로젝트 URL
   * @param resourceType 리소스 타입
   * @param payload 삭제할 템플릿 ID
   */
  async deleteTemplate(
    projectUrl: string,
    resourceType: ResourceType,
    payload: TemplateDeletePayload
  ): Promise<void> {
    const resourcePath = RESOURCE_MAP[resourceType].path
    await apiClient.delete(
      `${this.baseURL}/${projectUrl}/${resourcePath}/templates`,
      { data: payload }
    )
  },
}

//  실제 사용을 위한 명시적 함수 재정의

export const issueTemplateService = {
  // issueTemplateService의 함수들 (templateService.getTemplates(projectUrl, 'issues') 호출)
  async getTemplates(projectUrl: string): Promise<IssueTemplateListResponse> {
    return templateService.getTemplates(projectUrl, 'issues')
  },
  async getTemplateDetail(
    projectUrl: string,
    templateId: number
  ): Promise<TemplateDetail> {
    return templateService.getTemplateDetail(projectUrl, 'issues', templateId)
  },
  async createTemplate(
    projectUrl: string,
    payload: TemplateCreatePayload
  ): Promise<void> {
    return templateService.createTemplate(projectUrl, 'issues', payload)
  },
  async updateTemplate(
    projectUrl: string,
    payload: TemplateUpdatePayload
  ): Promise<void> {
    return templateService.updateTemplate(projectUrl, 'issues', payload)
  },
  async deleteTemplate(
    projectUrl: string,
    payload: TemplateDeletePayload
  ): Promise<void> {
    return templateService.deleteTemplate(projectUrl, 'issues', payload)
  },
}

export const meetingTemplateService = {
  // meetingTemplateService의 함수들
  async getTemplates(projectUrl: string): Promise<MeetingTemplateListResponse> {
    return templateService.getTemplates(projectUrl, 'meetings')
  },
  async getTemplateDetail(
    projectUrl: string,
    templateId: number
  ): Promise<TemplateDetail> {
    return templateService.getTemplateDetail(projectUrl, 'meetings', templateId)
  },
  async createTemplate(
    projectUrl: string,
    payload: TemplateCreatePayload
  ): Promise<void> {
    return templateService.createTemplate(projectUrl, 'meetings', payload)
  },
  async updateTemplate(
    projectUrl: string,
    payload: TemplateUpdatePayload
  ): Promise<void> {
    return templateService.updateTemplate(projectUrl, 'meetings', payload)
  },
  async deleteTemplate(
    projectUrl: string,
    payload: TemplateDeletePayload
  ): Promise<void> {
    return templateService.deleteTemplate(projectUrl, 'meetings', payload)
  },
}

export const retroTemplateService = {
  // retroTemplateService의 함수들
  async getTemplates(projectUrl: string): Promise<RetroTemplateListResponse> {
    return templateService.getTemplates(projectUrl, 'retros')
  },
  async getTemplateDetail(
    projectUrl: string,
    templateId: number
  ): Promise<TemplateDetail> {
    return templateService.getTemplateDetail(projectUrl, 'retros', templateId)
  },
  async createTemplate(
    projectUrl: string,
    payload: TemplateCreatePayload
  ): Promise<void> {
    return templateService.createTemplate(projectUrl, 'retros', payload)
  },
  async updateTemplate(
    projectUrl: string,
    payload: TemplateUpdatePayload
  ): Promise<void> {
    return templateService.updateTemplate(projectUrl, 'retros', payload)
  },
  async deleteTemplate(
    projectUrl: string,
    payload: TemplateDeletePayload
  ): Promise<void> {
    return templateService.deleteTemplate(projectUrl, 'retros', payload)
  },
}

export const scrumTemplateService = {
  // scrumTemplateService의 함수들
  async getTemplates(projectUrl: string): Promise<ScrumTemplateListResponse> {
    return templateService.getTemplates(projectUrl, 'scrums')
  },
  async getTemplateDetail(
    projectUrl: string,
    templateId: number
  ): Promise<TemplateDetail> {
    return templateService.getTemplateDetail(projectUrl, 'scrums', templateId)
  },
  async createTemplate(
    projectUrl: string,
    payload: TemplateCreatePayload
  ): Promise<void> {
    return templateService.createTemplate(projectUrl, 'scrums', payload)
  },
  async updateTemplate(
    projectUrl: string,
    payload: TemplateUpdatePayload
  ): Promise<void> {
    return templateService.updateTemplate(projectUrl, 'scrums', payload)
  },
  async deleteTemplate(
    projectUrl: string,
    payload: TemplateDeletePayload
  ): Promise<void> {
    return templateService.deleteTemplate(projectUrl, 'scrums', payload)
  },
}
