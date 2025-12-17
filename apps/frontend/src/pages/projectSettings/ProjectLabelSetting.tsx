import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Plus, MoreHorizontal } from 'lucide-react'
import { labelService } from '@/api/services/labelService'
import { LabelFormModal } from '@/components/label/LabelFormModal'
import type { Label } from '@/types/label'

export default function ProjectLabelSetting() {
  const { projectUrl } = useParams<{ projectUrl: string }>()

  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [modalMode, setModalMode] = useState<'create' | 'edit'>('create')
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
    setEditingLabel(null)
    setIsModalOpen(true)
  }

  const handleEditClick = (label: Label) => {
    setModalMode('edit')
    setEditingLabel(label)
    setIsModalOpen(true)
  }

  const handleModalClose = () => {
    setIsModalOpen(false)
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
    <div className="container flex flex-col justify-center items-center gap-8 p-10">
      {/* Title */}
      <div className="flex w-full max-w-[1100px] flex-col items-center gap-6">
        <h1 className="text-center text-[40px] font-bold leading-[48px] font-['Roboto'] pb-10">
          Label Setting
        </h1>

        {/* Tabs - 유저정보 페이지 수정을 위해 남겨둠 */}
        {/* <div className="flex h-[53px] w-full items-start">
          <div className="flex h-[53px] flex-1 items-center justify-center border-b border-[#6d758f] px-5 py-3">
            <p className="text-center text-sm text-[#6d758f]">labels</p>
          </div>
          <div className="h-[53px] flex-1 border-b border-[#e1e4ed]" />
        </div> */}
      </div>

      {/* Label List */}
      <div className="flex w-full flex-col gap-5 max-w-3xl">
        {data.labels.map(label => (
          <div
            key={label.id}
            className="flex h-[90px] items-center justify-between rounded-[10px] border-[1.7px] border-[#e1e4ed] bg-white px-9 py-5"
          >
            <div
              className="rounded-[10px] px-4 py-1"
              style={{ backgroundColor: label.color }}
            >
              <p className="text-[16px] font-medium leading-[28px] text-white">
                {label.name}
              </p>
            </div>
            <p className="text-center text-base text-[#6d758f]">
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

      {/* Unified Label Form Modal */}
      <LabelFormModal
        mode={modalMode}
        isOpen={isModalOpen}
        onClose={handleModalClose}
        currentLabel={editingLabel}
        projectUrl={projectUrl}
      />
    </div>
  )
}
