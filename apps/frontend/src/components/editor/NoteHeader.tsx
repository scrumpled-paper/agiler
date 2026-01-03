import { useState, useEffect } from 'react'
import type * as Y from 'yjs'

interface ScrumHeaderProps {
  ydoc: Y.Doc | undefined
  ytitle: Y.Text | undefined // y.text -> Y.Text (대문자 주의)
  type: string
  isConnected?: boolean
  isSynced?: boolean
  lastSaved?: Date
  isSaving?: boolean
}

export function NoteHeader({
  ydoc,
  ytitle,
  type,
  isConnected = false,
  // isSynced = false,
  lastSaved,
  // isSaving = false,
}: ScrumHeaderProps) {
  const [localTitle, setLocalTitle] = useState('')

  // 1. Yjs의 제목 변경 감지 (Remote -> Local)
  useEffect(() => {
    if (!ytitle) return

    // 초기값 로드
    setLocalTitle(ytitle.toString())

    const handleUpdate = () => {
      // 내가 입력 중일 때는 로컬 상태를 우선하되,
      // 데이터가 실제와 다를 때만 업데이트하여 커서 튀김 방지
      const remoteValue = ytitle.toString()
      if (remoteValue !== localTitle) {
        setLocalTitle(remoteValue)
      }
    }

    ytitle.observe(handleUpdate)
    return () => ytitle.unobserve(handleUpdate)
  }, [ytitle, localTitle])

  // 2. 제목 수정 핸들러 (Local -> Remote)
  const handleTitleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = e.target.value
    setLocalTitle(newValue)

    if (ydoc && ytitle) {
      // 트랜잭션으로 묶어서 원자적으로 업데이트
      ydoc.transact(() => {
        ytitle.delete(0, ytitle.length)
        ytitle.insert(0, newValue)
      })
    }
  }

  return (
    <div className="flex flex-col gap-2 mb-6 border-b pb-4">
      {/* 상태 표시 영역 (상단) */}
      <div className="flex justify-between items-center text-xs">
        <span className="font-semibold text-blue-600 uppercase tracking-wider">
          {type}
        </span>
        <div className="flex gap-4 items-center">
          <div className="flex items-center gap-1.5">
            <span
              className={`w-2 h-2 rounded-full ${isConnected ? 'bg-green-500' : 'bg-red-500'}`}
            />
            <span className="text-gray-500">
              {isConnected ? 'Live' : 'Offline'}
            </span>
          </div>
          {lastSaved && (
            <span className="text-gray-400">
              저장됨: {lastSaved.toLocaleTimeString()}
            </span>
          )}
        </div>
      </div>

      {/* 제목 입력 영역 (하단) */}
      <input
        value={localTitle}
        onChange={handleTitleChange}
        placeholder="제목 없는 문서"
        className="text-4xl font-extrabold bg-transparent border-none outline-none focus:ring-0 p-0 placeholder:text-gray-300 w-full"
      />
    </div>
  )
}
