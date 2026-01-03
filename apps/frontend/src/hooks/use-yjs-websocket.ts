import { useEffect, useRef, useState } from 'react'
import * as Y from 'yjs'
import { WebsocketProvider } from 'y-websocket'
import type { Participant, YjsConnectionState } from '@/types/yjs'
import { wssService, collabService } from '@/api/services/wssService'

interface UseYjsWebSocketOptions {
  projectUrl: string // 추가: Spring 서버 API 호출에 필요
  docId: string
  initialContent?: string
  initialParticipants?: Participant[]
  enabled?: boolean
  noteType: string
}

export function useYjsWebSocket({
  projectUrl,
  docId,
  // initialParticipants = [],
  enabled = true,
  noteType,
}: UseYjsWebSocketOptions) {
  // 기존 상태
  const [isConnected, setIsConnected] = useState(false)
  const [isSynced, setIsSynced] = useState(false)

  // 새로운 상태: 연결 과정 추적
  const [connectionState, setConnectionState] = useState<YjsConnectionState>({
    status: 'idle',
  })

  type ParticipantMap = Y.Map<string | number>

  const ydocRef = useRef<Y.Doc | undefined>(undefined)
  const providerRef = useRef<WebsocketProvider | undefined>(undefined)
  const ytitleRef = useRef<Y.Text | undefined>(undefined)
  const yParticipantsRef = useRef<Y.Array<ParticipantMap> | undefined>(
    undefined
  )

  useEffect(() => {
    if (!enabled || !projectUrl || !docId) return

    let ydoc: Y.Doc | null = null
    let provider: WebsocketProvider | null = null
    let tokenRefreshInterval: NodeJS.Timeout | null = null
    let isCleanedUp = false // cleanup 플래그 추가

    // 비동기 초기화 함수
    const initializeYjsConnection = async () => {
      try {
        // cleanup이 호출되었으면 초기화 중단
        if (isCleanedUp) return

        // 1단계: Yjs 문서 초기화
        ydoc = new Y.Doc()
        const ytitle = ydoc.getText('title')
        const yParticipants: Y.Array<ParticipantMap> =
          ydoc.getArray('participants')

        // cleanup이 호출되었으면 생성한 리소스 정리 후 종료
        if (isCleanedUp) {
          ydoc.destroy()
          return
        }

        ydocRef.current = ydoc
        ytitleRef.current = ytitle
        yParticipantsRef.current = yParticipants

        // 초기 참여자 목록 설정
        // if (initialParticipants.length > 0 && yParticipants.length === 0) {
        //   const yMaps = initialParticipants.map(participant => {
        //     const yMap: ParticipantMap = new Y.Map()
        //     yMap.set('profileId', participant.profileId)
        //     yMap.set('nickname', participant.nickname)
        //     yMap.set('imageUrl', participant.imageUrl)
        //     return yMap
        //   })
        //   yParticipants.push(yMaps)
        // }

        // 개발 환경에서 로컬 yjs-server 사용 (환경변수로 제어)
        const useLocalServer = import.meta.env.VITE_USE_LOCAL_YJS === 'true'

        if (useLocalServer) {
          // 로컬 개발 모드: 기존 방식 사용
          console.log('[YJS] 로컬 yjs-server 모드로 연결합니다.')

          // cleanup이 호출되었으면 중단
          if (isCleanedUp) return

          const wsUrl =
            import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:1234'
          const roomId = `${noteType}-${docId}`

          provider = new WebsocketProvider(wsUrl, roomId, ydoc, {
            connect: true,
          })
          console.log('provider : ', provider)
          console.error('provider : ', provider)

          // cleanup이 호출되었으면 provider 정리 후 종료
          if (isCleanedUp) {
            provider.disconnect()
            return
          }

          providerRef.current = provider

          // 로컬 모드 상태 리스너 등록
          const statusHandler = ({ status }: { status: string }) => {
            if (isCleanedUp) return

            console.log('[YJS] 연결 상태:', status)
            const connected = status === 'connected'
            setIsConnected(connected)

            if (connected) {
              setConnectionState({ status: 'connected' })
            }
          }

          const syncHandler = (synced: boolean) => {
            if (isCleanedUp) return

            console.log('[YJS] 동기화 상태:', synced)
            setIsSynced(synced)
          }

          provider.on('status', statusHandler)
          provider.on('sync', syncHandler)

          // 🔍 디버깅: Ydoc 업데이트 감지
          const updateHandler = (update: Uint8Array, origin: unknown) => {
            console.log('[YJS ✏️] 문서 업데이트 감지:', {
              크기: `${update.byteLength} bytes`,
              origin: origin === provider ? 'WebSocket' : 'Local',
              시간: new Date().toLocaleTimeString(),
            })
          }
          ydoc.on('update', updateHandler)
        } else {
          // 프로덕션 모드: 백엔드 WSS 서버 사용
          console.log('[YJS] 백엔드 WSS 서버 모드로 연결을 시작합니다.')

          // 토큰 기반 연결 함수 (초기 연결 + 재연결에 사용)
          const connectWithToken = async () => {
            // cleanup이 호출되었으면 중단
            if (isCleanedUp) return

            // 2단계: Spring 서버에서 wssToken 취득
            const meetingId = `meeting-${docId}`
            setConnectionState({ status: 'fetching-token' })
            const { wssToken } = await wssService.getWebSocketCredentials(
              projectUrl,
              meetingId
            )

            // cleanup이 호출되었으면 중단
            if (isCleanedUp) return

            console.log('[YJS] wssToken 취득 완료')

            // 3단계: WSS 서버에 Doc 생성 요청하여 roomId 취득
            setConnectionState({ status: 'creating-doc' })
            const { docId: roomId } =
              await collabService.createCollabServer(wssToken)

            // cleanup이 호출되었으면 중단
            if (isCleanedUp) return

            console.log('[YJS] docId:roomId 취득 완료:', roomId)

            // 기존 provider가 있으면 연결 해제
            if (provider) {
              console.log('[YJS] 기존 연결 해제 중...')
              provider.disconnect()
            }

            // cleanup이 호출되었으면 중단
            if (isCleanedUp) return

            // 4단계: WebsocketProvider로 실시간 연결
            setConnectionState({ status: 'connecting' })
            // WebSocket URL에 roomId를 포함시키고, provider에는 빈 문자열 전달
            // (y-websocket이 roomId를 경로에 추가하면 쿼리 파라미터 뒤에 붙어서 잘못된 URL이 됨)
            const wsUrl = collabService.getWebSocketUrl(wssToken, roomId)
            console.log('[YJS] WebSocket 연결 시작:', wsUrl)

            provider = new WebsocketProvider(wsUrl, '', ydoc!, {
              connect: true,
            })

            // cleanup이 호출되었으면 provider 정리 후 종료
            if (isCleanedUp) {
              provider.disconnect()
              return
            }

            console.log('provider : ', provider)
            providerRef.current = provider

            // 상태 리스너 등록
            const statusHandler = ({ status }: { status: string }) => {
              if (isCleanedUp) return

              console.log('[YJS] 연결 상태:', status)
              const connected = status === 'connected'
              setIsConnected(connected)

              if (connected) {
                setConnectionState({ status: 'connected' })
              }
            }

            const syncHandler = (synced: boolean) => {
              if (isCleanedUp) return

              console.log('[YJS] 동기화 상태:', synced)
              setIsSynced(synced)
            }

            provider.on('status', statusHandler)
            provider.on('sync', syncHandler)

            // 🔍 디버깅: Ydoc 업데이트 감지
            const updateHandler = (update: Uint8Array, origin: unknown) => {
              console.log('[YJS ✏️] 문서 업데이트 감지:', {
                크기: `${update.byteLength} bytes`,
                origin: origin === provider ? 'WebSocket' : 'Local',
                시간: new Date().toLocaleTimeString(),
              })
            }
            ydoc!.on('update', updateHandler)

            return provider
          }

          // 초기 연결
          await connectWithToken()

          // cleanup이 호출되었으면 중단
          if (isCleanedUp) return

          // 토큰 갱신 인터벌 설정 (4분 30초 = 270,000ms)
          tokenRefreshInterval = setInterval(
            async () => {
              // cleanup이 호출되었으면 인터벌 종료
              if (isCleanedUp) return

              try {
                console.log('[YJS] 토큰 갱신 중...')
                await connectWithToken()
                console.log('[YJS] 토큰 갱신 완료')
              } catch (error) {
                // cleanup이 호출되었으면 에러 무시
                if (isCleanedUp) return

                console.error('[YJS] 토큰 갱신 실패:', error)
                setConnectionState({
                  status: 'error',
                  error:
                    error instanceof Error
                      ? error
                      : new Error('토큰 갱신 실패'),
                })
              }
            },
            4.5 * 60 * 1000
          ) // 4분 30초
        }
      } catch (error) {
        console.error('[YJS] 연결 실패:', error)
        setConnectionState({
          status: 'error',
          error: error instanceof Error ? error : new Error('연결 실패'),
        })

        // 에러 발생 시 생성된 리소스 정리
        if (ydoc) {
          ydoc.destroy()
          ydocRef.current = undefined
        }
        if (provider) {
          provider.disconnect()
          providerRef.current = undefined
        }
      }
    }

    // 비동기 초기화 실행
    initializeYjsConnection()

    // 클린업 함수
    return () => {
      console.log('[YJS] 연결 정리 중...')

      // cleanup 플래그 설정 (비동기 작업 중단용)
      isCleanedUp = true

      // 토큰 갱신 인터벌 정리
      if (tokenRefreshInterval) {
        clearInterval(tokenRefreshInterval)
        console.log('[YJS] 토큰 갱신 인터벌 정리 완료')
      }

      if (providerRef.current) {
        providerRef.current.disconnect()
      }
      if (ydocRef.current) {
        ydocRef.current.destroy()
      }

      // 상태 초기화
      setIsConnected(false)
      setIsSynced(false)
      setConnectionState({ status: 'idle' })
    }
  }, [projectUrl, docId, noteType, enabled])

  return {
    ydoc: ydocRef.current,
    ytitle: ytitleRef.current,
    yParticipants: yParticipantsRef.current,
    provider: providerRef.current,
    isConnected,
    isSynced,
    connectionState, // 새로운 반환값: 연결 과정 상태
  }
}
