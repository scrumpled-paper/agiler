import * as Y from 'yjs'
import type { Participant } from '@/types/yjs'

type ParticipantMap = Y.Map<string | number>

/**
 * 참여자 추가 (중복 체크 포함)
 *
 * @param ydoc - Yjs 문서
 * @param participant - 추가할 참여자 정보
 * @returns 추가 성공 여부 (이미 존재하면 false)
 */
export function addParticipant(ydoc: Y.Doc, participant: Participant): boolean {
  const yParticipants = ydoc.getArray<ParticipantMap>('participants')

  // 중복 체크: 이미 같은 profileId가 있는지 확인
  const exists = yParticipants.toArray().some(yMap => {
    return yMap.get('profileId') === participant.profileId
  })

  if (exists) {
    console.warn(`참여자 ${participant.profileId}는 이미 추가되어 있습니다.`)
    return false
  }

  // Y.Map으로 변환하여 추가
  const yParticipant: ParticipantMap = new Y.Map()
  yParticipant.set('profileId', participant.profileId)
  yParticipant.set('nickname', participant.nickname)
  yParticipant.set('imageUrl', participant.imageUrl)

  yParticipants.push([yParticipant])
  return true
}

/**
 * 참여자 제거
 *
 * @param ydoc - Yjs 문서
 * @param profileId - 제거할 참여자의 profileId
 * @returns 제거 성공 여부 (찾을 수 없으면 false)
 */
export function removeParticipant(ydoc: Y.Doc, profileId: number): boolean {
  const yParticipants: Y.Array<ParticipantMap> = ydoc.getArray('participants')

  const index = yParticipants.toArray().findIndex(yMap => {
    return yMap.get('profileId') === profileId
  })

  if (index === -1) {
    console.warn(`참여자 ${profileId}를 찾을 수 없습니다.`)
    return false
  }

  yParticipants.delete(index, 1)
  return true
}

/**
 * 참여자 목록 조회
 *
 * @param ydoc - Yjs 문서
 * @returns 참여자 목록 배열
 */
export function getParticipants(ydoc: Y.Doc): Participant[] {
  const yParticipants: Y.Array<ParticipantMap> = ydoc.getArray('participants')

  return yParticipants.toArray().map(yMap => ({
    profileId: yMap.get('profileId') as number,
    nickname: yMap.get('nickname') as string,
    imageUrl: yMap.get('imageUrl') as string,
  }))
}

/**
 * 참여자 목록 변경 감지 (React에서 사용)
 *
 * @param ydoc - Yjs 문서
 * @param callback - 참여자 목록이 변경될 때 호출될 콜백 함수
 * @returns 구독 해제 함수
 */
export function observeParticipants(
  ydoc: Y.Doc,
  callback: (participants: Participant[]) => void
): () => void {
  const yParticipants = ydoc.getArray<ParticipantMap>('participants')

  const observer = () => {
    callback(getParticipants(ydoc))
  }

  yParticipants.observe(observer)

  // 클린업 함수 반환
  return () => yParticipants.unobserve(observer)
}

/**
 * 참여자 목록 전체 초기화
 *
 * @param ydoc - Yjs 문서
 * @param participants - 새로운 참여자 목록
 */
export function setParticipants(
  ydoc: Y.Doc,
  participants: Participant[]
): void {
  const yParticipants = ydoc.getArray<ParticipantMap>('participants')

  // 기존 목록 전체 삭제
  yParticipants.delete(0, yParticipants.length)

  // 새로운 목록 추가
  const yMaps = participants.map(participant => {
    const yMap: ParticipantMap = new Y.Map()
    yMap.set('profileId', participant.profileId)
    yMap.set('nickname', participant.nickname)
    yMap.set('imageUrl', participant.imageUrl)
    return yMap
  })

  yParticipants.push(yMaps)
}
