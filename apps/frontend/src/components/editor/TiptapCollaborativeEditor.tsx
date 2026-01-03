import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useEditor, EditorContent } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'
import Collaboration from '@tiptap/extension-collaboration'
import CollaborationCursor from '@tiptap/extension-collaboration-cursor'
import Placeholder from '@tiptap/extension-placeholder'
import type * as Y from 'yjs'
import type { WebsocketProvider } from 'y-websocket'
import type { UserInfo } from '@/types'
import type { Participant } from '@/types/yjs'
import { projectService } from '@/api/services/projectService'
import { AssigneeSelector } from '@/components/issue/AssigneeSelector'
import { SelectedAssignees } from '@/components/issue/SelectedAssignees'

interface TiptapCollaborativeEditorProps {
  ydoc: Y.Doc | undefined
  provider: WebsocketProvider | undefined
  currentUser?: {
    name: string
    color: string
  }
  placeholder?: string
  className?: string
  projectUrl?: string
  participants?: UserInfo[]
  onAddParticipant?: (member: Participant) => void
  onRemoveParticipant?: (profileId: number) => void
}

/**
 * Tiptap 기반 협업 에디터 컴포넌트
 *
 * Yjs Y.XmlFragment와 자동 동기화되는 실시간 협업 에디터입니다.
 * Collaboration extension이 Yjs와의 양방향 동기화를 자동으로 처리합니다.
 */
export function TiptapCollaborativeEditor({
  ydoc,
  provider,
  currentUser,
  placeholder = '내용을 입력하세요...',
  className = '',
  projectUrl,
  participants = [],
  onAddParticipant,
  onRemoveParticipant,
}: TiptapCollaborativeEditorProps) {
  // AssigneeSelector Popover 상태
  const [assigneePopoverOpen, setAssigneePopoverOpen] = useState(false)

  // 익명 사용자 정보 (랜덤 색상 포함)
  const [anonymousUser] = useState(() => ({
    name: 'Anonymous',
    color: '#' + Math.floor(Math.random() * 16777215).toString(16),
  }))

  // 프로젝트 멤버 조회 (Popover가 열릴 때만)
  const { data: membersData } = useQuery({
    queryKey: ['project-members', projectUrl],
    queryFn: () =>
      projectService.getProjectMember({
        projectUrl: projectUrl!,
        size: 100,
        page: 0,
      }),
    enabled: !!projectUrl && assigneePopoverOpen,
  })

  const editor = useEditor(
    {
      extensions: [
        // StarterKit: 기본 에디터 기능 (항상 필요)
        StarterKit.configure({
          // history는 Collaboration에서 관리하므로 비활성화
          history: false,
        }),
        // ydoc이 있을 때만 Collaboration extensions 추가
        ...(ydoc
          ? [
              // Collaboration: Yjs와 동기화
              Collaboration.configure({
                document: ydoc,
                field: 'content', // Y.Doc 내 필드 이름
              }),
              // CollaborationCursor: 다른 사용자의 커서 위치 표시
              CollaborationCursor.configure({
                provider: provider,
                user: currentUser || anonymousUser,
              }),
            ]
          : []),
        // Placeholder: 빈 에디터 placeholder
        Placeholder.configure({
          placeholder,
        }),
      ],
      editorProps: {
        attributes: {
          class: `prose prose-sm sm:prose lg:prose-lg focus:outline-none min-h-[500px] p-4 border rounded-md ${className}`,
        },
      },
    },
    [!!ydoc]
  )

  // ydoc이 없으면 로딩 표시
  if (!ydoc) {
    return (
      <div className="flex items-center justify-center min-h-[500px] border rounded-md bg-gray-50">
        <p className="text-gray-500">WebSocket 연결 중...</p>
      </div>
    )
  }

  if (!editor) {
    return (
      <div className="flex items-center justify-center min-h-[500px] border rounded-md bg-gray-50">
        <p className="text-gray-500">에디터를 초기화하는 중...</p>
      </div>
    )
  }

  // 프로젝트 멤버 목록
  const members = membersData?.contents || []

  // Participant를 UserInfo 타입으로 변환
  const participantsAsUserInfo: UserInfo[] = participants.map(p => ({
    profileId: p.profileId,
    nickname: p.nickname,
    imageUrl: p.imageUrl,
  }))

  // 참여자 추가 핸들러
  const handleAddAssignee = (member: UserInfo) => {
    onAddParticipant?.(member)
    setAssigneePopoverOpen(false)
  }

  // 참여자 제거 핸들러
  const handleRemoveAssignee = (profileId: number) => {
    onRemoveParticipant?.(profileId)
  }

  return (
    <div className="w-full">
      {/* 참여자 관리 UI */}
      {onAddParticipant && onRemoveParticipant && (
        <div className="mb-4 flex flex-col gap-2">
          <label className="text-sm font-bold text-black">참여자</label>
          <div className="flex flex-wrap items-center gap-2">
            <AssigneeSelector
              members={members}
              onAdd={handleAddAssignee}
              isOpen={assigneePopoverOpen}
              onOpenChange={setAssigneePopoverOpen}
            />
            <SelectedAssignees
              assignees={participantsAsUserInfo}
              onRemove={handleRemoveAssignee}
            />
          </div>
        </div>
      )}

      {/* 에디터 */}
      <EditorContent editor={editor} />
    </div>
  )
}
