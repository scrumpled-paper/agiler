import { useParams } from 'react-router-dom'
import { useYjsWebSocket } from '@/hooks/use-yjs-websocket'
import { useParticipants } from '@/hooks/use-participants'
import { addParticipant, removeParticipant } from '@/utils/yjs-participants'
import { TiptapCollaborativeEditor } from '@/components/editor/TiptapCollaborativeEditor'
// import { NoteHeader } from '@/components/scrum/ScrumHeader'
import type { Participant } from '@/types/yjs'
import { NoteHeader } from '@/components/editor/NoteHeader'

/**
 * Meeting 상세 페이지
 *
 * Tiptap 기반 협업 에디터를 사용하여 Meeting 문서를 실시간으로 편집합니다.
 * Yjs Doc으로만 데이터를 관리하며, 백엔드 API로 Meeting 데이터를 별도로 불러오지 않습니다.
 */
export default function Meeting() {
  const { projectUrl, meetingId } = useParams<{
    projectUrl: string
    meetingId: string
  }>()

  // WebSocket 연결 (Yjs Doc으로만 관리)
  const { ydoc, ytitle, provider, isConnected, isSynced, connectionState } =
    useYjsWebSocket({
      projectUrl: projectUrl!,
      docId: meetingId!,
      noteType: 'meeting',
      enabled: !!projectUrl && !!meetingId,
    })

  // 참여자 목록 (Yjs Y.Array에서 실시간 동기화)
  const participants = useParticipants(ydoc)

  // const participantsAsUserInfo: UserInfo[] = participants.map(p => ({
  //   profileId: p.profileId,
  //   nickname: p.nickname,
  //   imageUrl: p.imageUrl,
  // }))

  // 참여자 추가 핸들러
  const handleAddParticipant = (member: Participant) => {
    if (ydoc) {
      addParticipant(ydoc, {
        profileId: member.profileId,
        nickname: member.nickname,
        imageUrl: member.imageUrl,
      })
    }
  }

  // 참여자 제거 핸들러
  const handleRemoveParticipant = (profileId: number) => {
    if (ydoc) {
      removeParticipant(ydoc, profileId)
    }
  }

  // 로딩 상태 처리 (YJS 연결)
  if (
    connectionState.status === 'fetching-token' ||
    connectionState.status === 'creating-doc' ||
    connectionState.status === 'connecting'
  ) {
    return (
      <div className="container p-8">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="text-center">
            <p className="text-gray-500">
              {connectionState.status === 'fetching-token' &&
                '인증 토큰을 가져오는 중...'}
              {connectionState.status === 'creating-doc' &&
                '협업 세션을 생성하는 중...'}
              {connectionState.status === 'connecting' &&
                'WebSocket 서버에 연결하는 중...'}
            </p>
          </div>
        </div>
      </div>
    )
  }

  // 에러 상태 처리
  if (connectionState.status === 'error') {
    return (
      <div className="container p-8">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="text-center">
            <p className="text-red-500">
              협업 연결에 실패했습니다:{' '}
              {connectionState.error?.message || '알 수 없는 오류'}
            </p>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="container p-8">
      {/* 헤더: 제목, 연결 상태 */}
      <NoteHeader
        ydoc={ydoc}
        ytitle={ytitle}
        type="Meeting Note"
        isConnected={isConnected}
        isSynced={isSynced}
      />
      {/* Tiptap 협업 에디터 */}
      <div className="mt-6">
        <TiptapCollaborativeEditor
          ydoc={ydoc}
          provider={provider}
          placeholder="회의록을 작성해주세요..."
          projectUrl={projectUrl}
          participants={participants}
          onAddParticipant={handleAddParticipant}
          onRemoveParticipant={handleRemoveParticipant}
        />
      </div>
    </div>
  )
}
