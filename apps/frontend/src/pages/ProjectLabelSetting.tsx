import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Plus, MoreHorizontal } from 'lucide-react'
import { labelService } from '@/api/services/labelService'
import { LabelFormModal } from '@/components/LabelFormModal'
import type { Label } from '@/types/label'

export default function ProjectLabelSetting() {
  const { projectUrl } = useParams<{ projectUrl: string }>()

  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [modalMode, setModalMode] = useState<'create' | 'edit'>('create') // 모드 상태 추가

  // Edit states
  const [editingLabel, setEditingLabel] = useState<Label | null>(null)

  // Query to fetch labels
  const { data, isLoading, isError } = useQuery({
    queryKey: ['labels', projectUrl],
    queryFn: () => labelService.getLabels(projectUrl!),
    enabled: !!projectUrl,
  })

  // Handlers
  const handleCreateClick = () => {
    setModalMode('create')
    setEditingLabel(null) // 혹시 모를 경우를 대비해 초기화
    setIsModalOpen(true)
  }

  const handleEditClick = (label: Label) => {
    setModalMode('edit')
    setEditingLabel(label)
    setIsModalOpen(true)
  }

  // 모달 닫기/편집 완료 후 후처리
  const handleEditComplete = () => {
    // Edit 모드 작업 완료 후 editingLabel 상태 초기화
    setEditingLabel(null)
  }

  if (isLoading) {
    return (
      <div className="flex h-96 items-center justify-center">로딩 중...</div>
    )
  }

  if (isError || !data) {
    return <div className="container p-4">에러가 발생했습니다.</div>
  }

  return (
    <div className="container mx-auto flex flex-col items-center gap-8 p-8">
      {/* Title */}
      <div className="flex w-full max-w-[1100px] flex-col items-center gap-6">
        <h1 className="text-center text-[40px] font-bold leading-[48px] text-black">
          Label Setting
        </h1>
      </div>

      {/* Label List */}
      <div className="flex w-full max-w-[1100px] flex-col gap-4">
        {data.labels.map(label => (
          <div
            key={label.id}
            className="flex h-[90px] items-center justify-between rounded-[10px] border-[1.7px] border-[#e1e4ed] bg-white px-9 py-5"
          >
            <div
              className="rounded-[10px] px-4 py-1"
              style={{ backgroundColor: label.color }}
            >
              <p
                className="text-[16px] font-medium
              -[24px] text-white"
              >
                {label.name}
              </p>
            </div>
            <p className=" text-center text-base text-[#6d758f]">
              {label.description}
            </p>
            <button
              onClick={() => handleEditClick(label)}
              className="flex size-8 items-center justify-center transition-opacity hover:opacity-70"
            >
              <MoreHorizontal className="size-6 text-[#6d758f]" />
            </button>
          </div>
        ))}

        {/* Create New Label Button */}
        <button
          onClick={handleCreateClick}
          className="flex h-[90px] items-center justify-center rounded-[10px] border-[1.7px] border-[#e1e4ed] bg-white transition-colors hover:bg-gray-50"
        >
          <div className="flex size-[34px] rotate-45 items-center justify-center">
            <Plus className="size-6 rotate-[-45deg] text-[#6d758f]" />
          </div>
        </button>
      </div>

      {/* ✅ 통합 Label 폼 모달 */}
      <LabelFormModal
        mode={modalMode}
        isOpen={isModalOpen}
        onOpenChange={setIsModalOpen}
        currentLabel={editingLabel}
        onEditComplete={handleEditComplete}
      />
    </div>
  )
}
