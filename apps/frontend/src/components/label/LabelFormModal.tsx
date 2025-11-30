import { useState, useEffect } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
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

type LabelFormModalProps = {
  mode: 'create' | 'edit'
  isOpen: boolean
  onClose: () => void
  currentLabel: Label | null
  projectUrl: string | undefined
}

export function LabelFormModal({
  mode,
  isOpen,
  onClose,
  currentLabel,
  projectUrl,
}: LabelFormModalProps) {
  const queryClient = useQueryClient()

  // Form state
  const [formData, setFormData] = useState<LabelFormData>({
    name: '',
    description: '',
    color: '#000000',
  })

  // Reset form when modal opens or mode/currentLabel changes
  useEffect(() => {
    if (isOpen) {
      if (mode === 'edit' && currentLabel) {
        setFormData({
          name: currentLabel.name,
          description: currentLabel.description,
          color: currentLabel.color,
        })
      } else {
        setFormData({
          name: '',
          description: '',
          color: '#000000',
        })
      }
    }
  }, [isOpen, mode, currentLabel])

  // Create mutation
  const createMutation = useMutation({
    mutationFn: (payload: LabelCreateParams) => {
      if (!projectUrl) throw new Error('Project URL is required')
      return labelService.createLabel(projectUrl, payload)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['labels', projectUrl] })
      onClose()
    },
    onError: error => {
      console.error('Failed to create label:', error)
      alert('라벨 생성에 실패했습니다.')
    },
  })

  // Update mutation
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
      onClose()
    },
    onError: error => {
      console.error('Failed to update label:', error)
      alert('라벨 수정에 실패했습니다.')
    },
  })

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: (labelId: number) => {
      if (!projectUrl) throw new Error('Project URL is required')
      return labelService.deleteLabel(projectUrl, { labelId })
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['labels', projectUrl] })
      onClose()
    },
    onError: error => {
      console.error('Failed to delete label:', error)
      alert('라벨 삭제에 실패했습니다.')
    },
  })

  // Handlers
  const handleSubmit = () => {
    if (!formData.name.trim()) {
      alert('라벨 이름을 입력해주세요.')
      return
    }

    if (mode === 'create') {
      createMutation.mutate(formData)
    } else if (currentLabel) {
      updateMutation.mutate({
        labelId: currentLabel.id,
        payload: formData,
      })
    }
  }

  const handleDelete = () => {
    if (!currentLabel) return
    if (confirm(`정말 '${currentLabel.name}' 라벨을 삭제하시겠습니까?`)) {
      deleteMutation.mutate(currentLabel.id)
    }
  }

  const generateRandomColor = () => {
    const randomColor = `#${Math.floor(Math.random() * 16777215)
      .toString(16)
      .padStart(6, '0')}`
    setFormData({ ...formData, color: randomColor })
  }

  const selectColor = () => {
    const color = prompt('색상 코드를 입력하세요 (예: #FF4040):', formData.color)
    if (color && /^#[0-9A-F]{6}$/i.test(color)) {
      setFormData({ ...formData, color })
    } else if (color) {
      alert('올바른 색상 코드를 입력해주세요.')
    }
  }

  const isPending =
    createMutation.isPending ||
    updateMutation.isPending ||
    deleteMutation.isPending

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-[523px]" showCloseButton={false}>
        <DialogHeader>
          <DialogTitle className="sr-only">
            {mode === 'create' ? 'Create Label' : 'Edit Label'}
          </DialogTitle>
        </DialogHeader>

        <div className="flex flex-col gap-6">
          {/* Name Field */}
          <div className="flex flex-col gap-3">
            <label className="text-sm font-semibold text-[#6d758f]">Name</label>
            <Input
              placeholder="Label name"
              value={formData.name}
              onChange={e => setFormData({ ...formData, name: e.target.value })}
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
                value={formData.color}
                onChange={e =>
                  setFormData({ ...formData, color: e.target.value })
                }
                className="border-[#f1f3f7]"
                style={mode === 'edit' ? { color: formData.color } : undefined}
              />
            </div>
            <Button
              onClick={mode === 'create' ? selectColor : generateRandomColor}
              className="h-[48px] bg-[#6d758f] px-[18px] py-[14px] text-sm font-semibold text-white hover:bg-[#6d758f]/90"
            >
              {mode === 'create' ? '색상 선택' : '랜덤 선택'}
            </Button>
          </div>

          {/* Description Field */}
          <div className="flex flex-col gap-3">
            <label className="text-sm font-semibold text-[#6d758f]">
              Description
            </label>
            <Textarea
              placeholder=""
              value={formData.description}
              onChange={e =>
                setFormData({ ...formData, description: e.target.value })
              }
              className="h-[92px] resize-none border-[#f1f3f7]"
            />
          </div>
        </div>

        <DialogFooter className="gap-6">
          {mode === 'edit' ? (
            <>
              <Button
                variant="destructive"
                onClick={handleDelete}
                disabled={isPending}
                className="bg-[#e86c6c] px-[18px] py-[14px] text-sm font-semibold text-white hover:bg-[#e86c6c]/90"
              >
                Delete
              </Button>
              <Button
                onClick={handleSubmit}
                disabled={isPending}
                className="bg-[#6d758f] px-[18px] py-[14px] text-sm font-semibold text-white hover:bg-[#6d758f]/90"
              >
                Save
              </Button>
            </>
          ) : (
            <>
              <Button
                variant="destructive"
                onClick={onClose}
                className="bg-[#e86c6c] px-[18px] py-[14px] text-sm font-semibold text-white hover:bg-[#e86c6c]/90"
              >
                Cancel
              </Button>
              <Button
                onClick={handleSubmit}
                disabled={isPending}
                className="bg-[#6d758f] px-[18px] py-[14px] text-sm font-semibold text-white hover:bg-[#6d758f]/90"
              >
                Create
              </Button>
            </>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
