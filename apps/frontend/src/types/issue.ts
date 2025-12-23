import type { IssueColumn, UserInfo } from '@/types'
import type { Label } from './label'

// 백엔드 응답 타입 (API에서 실제로 받는 데이터)
export interface IssueResponse {
  assignees?: number[]
  // assignees?: UserInfo[]
  createdAt: string
  dueAt?: string
  isDone: boolean
  issueId: number
  kanbanConfigId: number
  labels?: number[]
  notis?: number[]
  // labels?: Label[]
  // notis?: UserInfo[]
  startedAt?: string
  title: string
}

// 프론트엔드에서 사용하는 Issue 타입 (KanbanItemProps 호환)
export interface Issue extends Record<string, unknown> {
  // KanbanItemProps 호환을 위한 속성들
  id: string // issueId와 동일
  name: string // title과 동일
  column: string // String(kanbanConfigId)

  // Issue 고유 속성들
  assignees: number[]
  // assignees: UserInfo[]
  createdAt: string
  dueAt: string
  isDone: boolean
  issueId: string
  kanbanConfigId: number
  labels?: number[]
  notis?: number[] // 알림 구독자 목록
  // labels?: Label[]
  // notis?: UserInfo[] // 알림 구독자 목록
  startedAt: string
  title: string
}

export interface updateIssuePayload {
  issueId: number
  title: string
  contents: string
  startedAt: string // '2025-12-17T06:54:24.637Z'
  dueAt: string // '2025-12-17T06:54:24.637Z'
}

export interface IssuePayload {
  title: string
  contents: string
  startedAt: string
  dueAt: string
  assignees?: number[] // 알림 구독자 목록
  labels?: number[]
}
// 백엔드 API 전체 응답 타입
export interface GetFilteredIssuesResponse {
  issues: IssueResponse[]
  kanbanConfigs: kanbanConfigs[]
  labels: Label[]
  profiles: UserInfo[]
}

export interface kanbanConfigs {
  kanbanConfigId: number
  statusName: string
  priority: number
  isDefault: boolean
  backlog: boolean
  isDone: boolean
}

export function toIssueColumns(configs: kanbanConfigs[]): IssueColumn[] {
  if (!configs) return []
  return configs
    .sort((a, b) => a.priority - b.priority) // 우선순위 정렬
    .map(config => ({
      id: String(config.kanbanConfigId),
      name: config.statusName,
      // 컬러는 백엔드에서 오지 않는다면 기본값을 주거나, 특정 ID별로 매핑 가능
      color: config.isDone ? '#10B981' : config.backlog ? '#6B7280' : '#3B82F6',
    }))
}

/**
 * 백엔드 응답을 프론트엔드 Issue 타입으로 변환
 */
export function toIssue(response: IssueResponse): Issue {
  return {
    ...response,
    // UI 라이브러리/컴포넌트 호환용 (기존 필드)
    id: String(response.issueId),
    name: response.title,
    column: String(response.kanbanConfigId),

    // 신규 API 명세 준수 (수정 후 인터페이스 필드)
    issueId: String(response.issueId),
    title: response.title,
    kanbanConfigId: response.kanbanConfigId,
    assignees: response.assignees || [],
    notis: response.notis || [],
    startedAt: response.startedAt || '',
    dueAt: response.dueAt || '',
    isDone: response.isDone ?? false,
    labels: response.labels || [],
  }
}

/**
 * IssueResponse 배열을 Issue 배열로 변환
 */
export function toIssues(responses: IssueResponse[] | undefined): Issue[] {
  if (!responses || !Array.isArray(responses)) {
    return []
  }
  return responses.map(toIssue)
}
