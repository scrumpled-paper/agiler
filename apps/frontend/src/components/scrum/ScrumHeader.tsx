// [ ] noteheader ui수정을 위해 남겨둠
import type * as Y from 'yjs'
interface ScrumHeaderProps {
  ydoc: Y.Doc
  // ytitle: Y.text
  type: string
  isConnected?: boolean
  isSynced?: boolean
  lastSaved?: Date
  isSaving?: boolean
}

/**
 * 동시편집 헤더 컴포넌트
 * - 제목 표시
 * - WebSocket 연결 상태 표시
 * - 마지막 저장 시간 표시
 */
export function NoteHeader({
  // ydoc,
  // ytitle,
  type,
  isConnected = false,
  isSynced = false,
  lastSaved,
  isSaving = false,
}: ScrumHeaderProps) {
  return (
    <div className="flex justify-between items-center mb-4">
      <h1 className="text-3xl font-bold">{type}</h1>

      <div className="flex gap-3 items-center text-sm">
        {/* WebSocket 연결 상태 */}
        <div className="flex items-center gap-1.5">
          <span
            className={`inline-block w-2 h-2 rounded-full ${
              isConnected ? 'bg-green-600' : 'bg-gray-400'
            }`}
          />
          <span className="text-muted-foreground">
            {isConnected ? '연결됨' : '연결 끊김'}
          </span>
        </div>

        {/* 동기화 상태 */}
        {isConnected && (
          <span className="text-muted-foreground">
            {isSynced ? '동기화됨' : '동기화 중...'}
          </span>
        )}

        {/* 저장 상태 */}
        {isSaving && <span className="text-muted-foreground">저장 중...</span>}

        {/* 마지막 저장 시간 */}
        {lastSaved && !isSaving && (
          <span className="text-muted-foreground">
            마지막 저장: {lastSaved.toLocaleTimeString('ko-KR')}
          </span>
        )}
      </div>
    </div>
  )
}
