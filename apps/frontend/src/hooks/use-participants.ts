import { useEffect, useState } from 'react'
import type * as Y from 'yjs'
import type { Participant } from '@/types/yjs'
import { getParticipants, observeParticipants } from '@/utils/yjs-participants'

/**
 * Yjs Y.Array에서 참여자 목록을 실시간으로 동기화하는 훅
 */
export function useParticipants(ydoc: Y.Doc | undefined) {
  const [participants, setParticipants] = useState<Participant[]>([])

  useEffect(() => {
    if (!ydoc) {
      setParticipants([])
      return
    }

    // 초기 로드
    const initialParticipants = getParticipants(ydoc)
    console.log('[YJS] 참여자 초기 로드:', initialParticipants.length, '명')
    setParticipants(initialParticipants)

    // 변경 감지 및 실시간 동기화
    const unobserve = observeParticipants(ydoc, newParticipants => {
      console.log('[YJS] 참여자 변경 감지:', newParticipants.length, '명')
      setParticipants(newParticipants)
    })

    return unobserve
  }, [ydoc])

  return participants
}
