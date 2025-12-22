import type { Label, UserInfo } from '@/types'

export interface Issue extends Record<string, unknown> {
  id: string
  name: string
  startAt: Date
  endAt: Date
  column: string // TaskColumn의 id
  owner?: UserInfo
  subscribers?: UserInfo[] // 알림 구독자 목록
  labels?: Label[]
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
  assignees?: UserInfo[] // 알림 구독자 목록
  labels?: Label[]
}
