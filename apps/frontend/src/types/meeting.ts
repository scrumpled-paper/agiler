// [ ] 실제 서버로 변경 후 없어질 파일
import type { Participant } from './activity'

/**
 * Meeting 상세 정보
 */
export interface Meeting {
  id: number
  title: string
  contents: string
  createdAt: string
  participants: Participant[]
}

/**
 * Meeting 내용 업데이트 페이로드
 */
export interface UpdateMeetingPayload {
  contents: string
}
