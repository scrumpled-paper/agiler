import type * as Y from 'yjs'
import type { WebsocketProvider } from 'y-websocket'

/**
 * Yjs 연결 정보
 */
type ParticipantMap = Y.Map<string | number>
export interface YjsConnection {
  ydoc: Y.Doc
  ytitle: Y.Text
  yParticipants: Y.Array<ParticipantMap>
  provider: WebsocketProvider
  isConnected: boolean
  isSynced: boolean
}

/**
 * Yjs Awareness 사용자 정보 (실시간 참여자)
 */
export interface AwarenessUser {
  profileId: number
  nickname: string
  imageUrl: string
  color: string // 사용자 구분 색상
}

/**
 * WebSocket 토큰 발급 API 응답
 * (NestJS 서버 구현 후 실제 사용)
 */
export interface WebSocketCredentials {
  wssToken: string // WebSocket 인증 토큰
}

/**
 * 참여자 정보
 * (사용자가 선택하여 추가하는 참여자)
 */
export interface Participant {
  profileId: number
  nickname: string
  imageUrl: string
}

/**
 * WSS 서버 Doc 생성 API 응답
 */
export interface CreateCollabServerResponse {
  docId: string // WSS 서버에서 반환하는 방 ID
  userId: string
  ready: boolean
}

/**
 * YJS 연결 상태 추적
 */
export interface YjsConnectionState {
  status:
    | 'idle' // 초기 상태
    | 'fetching-token' // Spring 서버에서 토큰 취득 중
    | 'creating-doc' // WSS 서버에 Doc 생성 중
    | 'connecting' // WebSocket 연결 중
    | 'connected' // 연결 완료
    | 'error' // 에러 발생
  error?: Error // 에러 객체 (에러 발생 시)
}
