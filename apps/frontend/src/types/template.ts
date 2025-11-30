// src/types/template.ts

//  * 범용 템플릿 목록 항목 (Meeting, Retro, Scrum 모두 사용 가능)
export type TemplateListItem = {
  templateId: number
  title: string
  description: string
}

export type IssueTemplateListResponse = {
  issueTemplates: TemplateListItem[]
  size: number
}
export type MeetingTemplateListResponse = {
  meetingTemplates: TemplateListItem[]
  size: number
}

export type RetroTemplateListResponse = {
  retroTemplates: TemplateListItem[]
  size: number
}

export type ScrumTemplateListResponse = {
  scrumTemplates: TemplateListItem[]
  size: number
}

export type TemplateListResponse =
  | MeetingTemplateListResponse
  | RetroTemplateListResponse
  | ScrumTemplateListResponse

/**
 * 템플릿 상세 내용 및 생성/수정 요청 본문 (Contents 포함)
 */
export type TemplateDetail = {
  title: string
  description: string
  contents: string // 템플릿 본문 내용
}

/**
 * POST 요청 (생성)
 */
export type TemplateCreatePayload = TemplateDetail

/**
 * PUT 요청 (수정)
 */
export type TemplateUpdatePayload = TemplateDetail & {
  templateId: number // 수정 시 ID 필수
}

/**
 * DELETE 요청 (삭제)
 */
export type TemplateDeletePayload = {
  templateId: number
}
