import type {
  WebSocketCredentials,
  CreateCollabServerResponse,
} from '@/types/yjs'
import { apiClient } from '../client'
import axios from 'axios'

/**
 * WSS 토큰 발급 서비스
 * Spring 서버에서 WebSocket 인증 토큰을 받아옵니다.
 */
export const wssService = {
  url: '/api/v1/projects',

  /**
   * WebSocket 토큰 발급
   * @param projectUrl - 프로젝트 URL
   * @param docId - 문서 ID (meetingId, retroId, scrumId 등)
   * @returns wssToken을 포함한 인증 정보
   */
  async getWebSocketCredentials(
    projectUrl: string,
    docId: string
  ): Promise<WebSocketCredentials> {
    const wssUrl = `${this.url}/${projectUrl}/${docId}`
    const response = await apiClient.get<WebSocketCredentials>(wssUrl)
    return response.data
  },
}
/**
 * WSS 서버 협업 Doc 생성 서비스
 * WSS 서버에 토큰을 전달하고 roomId를 받아옵니다.
 */
export const collabService = {
  /**
   * 협업 서버에 Doc 생성/검증 요청
   * @param wssToken - Spring 서버에서 받은 WebSocket 토큰
   * @returns roomId를 포함한 응답
   */
  async createCollabServer(
    wssToken: string
  ): Promise<CreateCollabServerResponse> {
    // const response = await apiClient.post<CreateCollabServerResponse>(
    //   '/collab/api/v1/wss/verify',
    //   { wssToken }
    // )
    const response = await axios.post<CreateCollabServerResponse>(
      'https://agiler.p-e.kr/collab/api/v1/wss/verify',
      { wssToken }
    )
    return response.data
  },

  /**
   * WebSocket URL 생성
   * @param wssToken - WebSocket 인증 토큰
   * @param roomId - 방 ID (선택사항)
   * @returns 연결할 WebSocket URL
   */
  getWebSocketUrl(wssToken: string, roomId?: string): string {
    const wsProtocol = import.meta.env.VITE_WS_PROTOCOL || 'wss'
    // const wsHost = import.meta.env.VITE_WS_HOST || 'agiler.p-e.kr'
    // const wsPort = import.meta.env.VITE_WS_PORT || '1234'

    // roomId가 있으면 경로에 포함, 없으면 쿼리 파라미터만
    const path = roomId ? `/ws/${roomId}` : '/ws/'
    return `${wsProtocol}://agiler.p-e.kr${path}?wssToken=${wssToken}`
  },
}
