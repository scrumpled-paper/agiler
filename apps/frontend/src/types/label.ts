/**
 * GET /api/v1/projects/{projectUrl}/labels 응답의 단일 레이블 객체
 */
export type Label = {
  labelId: number
  name: string
  description: string
  color: string // 예: "#4bB68C"
}

/**
 * GET /api/v1/projects/{projectUrl}/labels 응답 전체 스키마
 */
export type LabelListResponse = {
  labels: Label[]
  size: number
}

/**
 * POST /api/v1/projects/{projectUrl}/labels 요청 body
 */
export type LabelCreateParams = {
  name: string
  description: string
  color: string
}

/**
 * PUT /api/v1/projects/{projectUrl}/labels/{labelId} 요청 body
 * (LabelCreateParams와 동일할 수 있지만, 명시적으로 분리)
 */
export type LabelUpdateParams = {
  name: string
  description: string
  color: string
}

/**
 * DELETE /api/v1/projects/{projectUrl}/labels 요청 body
 */
export type LabelDeleteParams = {
  labelId: number
}
