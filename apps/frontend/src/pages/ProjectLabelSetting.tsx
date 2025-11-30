import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, MoreHorizontal } from 'lucide-react'
import { labelService } from '@/api/services/labelService'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import type { Label, LabelCreateParams, LabelUpdateParams } from '@/types/label'

type LabelFormData = {
  name: string
  description: string
  color: string
}

export default function ProjectLabelSetting() {
  const { projectUrl } = useParams<{ projectUrl: string }>()
  const queryClient = useQueryClient()

  // Modal states
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false)
  const [isEditModalOpen, setIsEditModalOpen] = useState(false)
  const [editingLabel, setEditingLabel] = useState<Label | null>(null)

  // Form states
  const [createForm, setCreateForm] = useState<LabelFormData>({
    name: '',
    description: '',
    color: '#000000',
  })
  const [editForm, setEditForm] = useState<LabelFormData>({
    name: '',
    description: '',
    color: '#000000',
  })

  // Query to fetch labels
  const { data, isLoading, isError } = useQuery({
    queryKey: ['labels', projectUrl],
    queryFn: () => labelService.getLabels(projectUrl!),
    enabled: !!projectUrl,
  })

  // Create label mutation
  const createMutation = useMutation({
    mutationFn: (payload: LabelCreateParams) => {
      if (!projectUrl) throw new Error('Project URL is required')
      return labelService.createLabel(projectUrl, payload)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['labels', projectUrl] })
      setIsCreateModalOpen(false)
      setCreateForm({ name: '', description: '', color: '#000000' })
    },
    onError: error => {
      console.error('Failed to create label:', error)
      alert('라벨 생성에 실패했습니다.')
    },
  })

  // Update label mutation
  const updateMutation = useMutation({
    mutationFn: ({
      labelId,
      payload,
    }: {
      labelId: number
      payload: LabelUpdateParams
    }) => {
      if (!projectUrl) throw new Error('Project URL is required')
      return labelService.updateLabel(projectUrl, labelId, payload)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['labels', projectUrl] })
      setIsEditModalOpen(false)
      setEditingLabel(null)
    },
    onError: error => {
      console.error('Failed to update label:', error)
      alert('라벨 수정에 실패했습니다.')
    },
  })

  // Delete label mutation
  const deleteMutation = useMutation({
    mutationFn: (labelId: number) => {
      if (!projectUrl) throw new Error('Project URL is required')
      return labelService.deleteLabel(projectUrl, { labelId })
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['labels', projectUrl] })
      setIsEditModalOpen(false)
      setEditingLabel(null)
    },
    onError: error => {
      console.error('Failed to delete label:', error)
      alert('라벨 삭제에 실패했습니다.')
    },
  })

  // Handlers
  const handleCreateClick = () => {
    setIsCreateModalOpen(true)
  }

  const handleEditClick = (label: Label) => {
    setEditingLabel(label)
    setEditForm({
      name: label.name,
      description: label.description,
      color: label.color,
    })
    setIsEditModalOpen(true)
  }

  const handleCreateSubmit = () => {
    if (!createForm.name.trim()) {
      alert('라벨 이름을 입력해주세요.')
      return
    }
    createMutation.mutate(createForm)
  }

  const handleEditSubmit = () => {
    if (!editingLabel) return
    if (!editForm.name.trim()) {
      alert('라벨 이름을 입력해주세요.')
      return
    }
    updateMutation.mutate({
      labelId: editingLabel.id,
      payload: editForm,
    })
  }

  const handleDelete = () => {
    if (!editingLabel) return
    if (confirm(`정말 '${editingLabel.name}' 라벨을 삭제하시겠습니까?`)) {
      deleteMutation.mutate(editingLabel.id)
    }
  }

  const generateRandomColor = () => {
    const randomColor = `#${Math.floor(Math.random() * 16777215)
      .toString(16)
      .padStart(6, '0')}`
    setEditForm({ ...editForm, color: randomColor })
  }

  const selectColor = () => {
    // Color picker functionality - you could implement a color picker here
    const color = prompt(
      '색상 코드를 입력하세요 (예: #FF4040):',
      createForm.color
    )
    if (color && /^#[0-9A-F]{6}$/i.test(color)) {
      setCreateForm({ ...createForm, color })
    } else if (color) {
      alert('올바른 색상 코드를 입력해주세요.')
    }
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

        {/* Tabs */}
        <div className="flex h-[53px] w-full items-start">
          <div className="flex h-[53px] flex-1 items-center justify-center border-b border-[#6d758f] px-5 py-3">
            <p className="text-center text-sm text-[#6d758f]">labels</p>
          </div>
          <div className="h-[53px] flex-1 border-b border-[#e1e4ed]" />
        </div>
      </div>

      {/* Label List */}
      <div className="flex w-full max-w-[1100px] flex-col gap-6">
        {data.labels.map(label => (
          <div
            key={label.id}
            className="flex h-[97px] items-center justify-between rounded-[10px] border-[1.7px] border-[#e1e4ed] bg-white px-9 py-10"
          >
            <div
              className="rounded-[10px] px-4 py-1"
              style={{ backgroundColor: label.color }}
            >
              <p className="text-[24px] font-medium leading-[34px] text-white">
                {label.name}
              </p>
            </div>
            <p className="w-[558px] text-center text-base text-[#6d758f]">
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
          className="flex h-[124px] items-center justify-center rounded-[10px] border-[1.7px] border-[#e1e4ed] bg-white transition-colors hover:bg-gray-50"
        >
          <div className="flex size-[34px] rotate-45 items-center justify-center">
            <Plus className="size-6 rotate-[-45deg] text-[#6d758f]" />
          </div>
        </button>
      </div>

      {/* Create Label Modal */}
      <Dialog open={isCreateModalOpen} onOpenChange={setIsCreateModalOpen}>
        <DialogContent className="max-w-[523px]" showCloseButton={false}>
          <DialogHeader>
            <DialogTitle className="sr-only">Create Label</DialogTitle>
          </DialogHeader>
          <div className="flex flex-col gap-6">
            {/* Name Field */}
            <div className="flex flex-col gap-3">
              <label className="text-sm font-semibold text-[#6d758f]">
                Name
              </label>
              <Input
                placeholder="Label name"
                value={createForm.name}
                onChange={e =>
                  setCreateForm({ ...createForm, name: e.target.value })
                }
                className="border-[#f1f3f7]"
              />
            </div>

            {/* Color Field */}
            <div className="flex items-end gap-6">
              <div className="flex flex-1 flex-col gap-3">
                <label className="text-sm font-semibold text-[#6d758f]">
                  Color
                </label>
                <Input
                  placeholder="#000000"
                  value={createForm.color}
                  onChange={e =>
                    setCreateForm({ ...createForm, color: e.target.value })
                  }
                  className="border-[#f1f3f7]"
                />
              </div>
              <Button
                onClick={selectColor}
                className="h-[48px] bg-[#6d758f] px-[18px] py-[14px] text-sm font-semibold text-white hover:bg-[#6d758f]/90"
              >
                색상 선택
              </Button>
            </div>

            {/* Description Field */}
            <div className="flex flex-col gap-3">
              <label className="text-sm font-semibold text-[#6d758f]">
                Description
              </label>
              <Textarea
                placeholder=""
                value={createForm.description}
                onChange={e =>
                  setCreateForm({ ...createForm, description: e.target.value })
                }
                className="h-[92px] resize-none border-[#f1f3f7]"
              />
            </div>
          </div>

          <DialogFooter className="gap-6">
            <Button
              variant="destructive"
              onClick={() => setIsCreateModalOpen(false)}
              className="bg-[#e86c6c] px-[18px] py-[14px] text-sm font-semibold text-white hover:bg-[#e86c6c]/90"
            >
              Cancel
            </Button>
            <Button
              onClick={handleCreateSubmit}
              disabled={createMutation.isPending}
              className="bg-[#6d758f] px-[18px] py-[14px] text-sm font-semibold text-white hover:bg-[#6d758f]/90"
            >
              Create
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit Label Modal */}
      <Dialog open={isEditModalOpen} onOpenChange={setIsEditModalOpen}>
        <DialogContent className="max-w-[523px]" showCloseButton={false}>
          <DialogHeader>
            <DialogTitle className="sr-only">Edit Label</DialogTitle>
          </DialogHeader>
          <div className="flex flex-col gap-6">
            {/* Name Field */}
            <div className="flex flex-col gap-3">
              <label className="text-sm font-semibold text-[#6d758f]">
                Name
              </label>
              <Input
                value={editForm.name}
                onChange={e =>
                  setEditForm({ ...editForm, name: e.target.value })
                }
                className="border-[#f1f3f7]"
              />
            </div>

            {/* Color Field */}
            <div className="flex items-end gap-6">
              <div className="flex flex-1 flex-col gap-3">
                <label className="text-sm font-semibold text-[#6d758f]">
                  Color
                </label>
                <Input
                  value={editForm.color}
                  onChange={e =>
                    setEditForm({ ...editForm, color: e.target.value })
                  }
                  className="border-[#f1f3f7]"
                  style={{ color: editForm.color }}
                />
              </div>
              <Button
                onClick={generateRandomColor}
                className="h-[48px] bg-[#6d758f] px-[18px] py-[14px] text-sm font-semibold text-white hover:bg-[#6d758f]/90"
              >
                랜덤 선택
              </Button>
            </div>

            {/* Description Field */}
            <div className="flex flex-col gap-3">
              <label className="text-sm font-semibold text-[#6d758f]">
                Description
              </label>
              <Textarea
                value={editForm.description}
                onChange={e =>
                  setEditForm({ ...editForm, description: e.target.value })
                }
                className="h-[92px] resize-none border-[#f1f3f7]"
              />
            </div>
          </div>

          <DialogFooter className="gap-6">
            <Button
              variant="destructive"
              onClick={handleDelete}
              disabled={deleteMutation.isPending}
              className="bg-[#e86c6c] px-[18px] py-[14px] text-sm font-semibold text-white hover:bg-[#e86c6c]/90"
            >
              Delete
            </Button>
            <Button
              onClick={handleEditSubmit}
              disabled={updateMutation.isPending}
              className="bg-[#6d758f] px-[18px] py-[14px] text-sm font-semibold text-white hover:bg-[#6d758f]/90"
            >
              Save
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
