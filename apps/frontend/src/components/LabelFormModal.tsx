// src/components/modals/LabelFormModal.tsx

import { useState, useEffect } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useParams } from 'react-router-dom'
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
  // 모드의 구분: 'create' 또는 'edit'
  mode: 'create' | 'edit'
  isOpen: boolean
  onOpenChange: (open: boolean) => void
  // 'edit' 모드일 때 현재 편집 중인 라벨 데이터
  currentLabel: Label | null
  // 편집 라벨 상태를 초기화하기 위한 함수 (메인 컴포넌트에서 전달)
  onEditComplete: () => void
}

const initialFormState: LabelFormData = {
  name: '',
  description: '',
  color: '#000000',
}

export function LabelFormModal({
  mode,
  isOpen,
  onOpenChange,
  currentLabel,
  onEditComplete,
}: LabelFormModalProps) {
  const { projectUrl } = useParams<{ projectUrl: string }>()
  const queryClient = useQueryClient()

  // 폼 상태: 생성/수정 모두 이 상태를 사용
  const [form, setForm] = useState<LabelFormData>(initialFormState)

  // 폼 초기화 (Edit 모드일 때 currentLabel 값으로, Create 모드일 때 초기값으로 설정)
  useEffect(() => {
    if (mode === 'edit' && currentLabel) {
      setForm({
        name: currentLabel.name,
        description: currentLabel.description,
        color: currentLabel.color,
      })
    } else if (mode === 'create') {
      setForm(initialFormState)
    }
  }, [mode, currentLabel])

  // UI 텍스트 정의
  const dialogTitle = mode === 'create' ? 'Create Label' : 'Edit Label'
  const submitButtonText = mode === 'create' ? 'Create' : 'Save'

  // 성공 시 공통 처리 함수
  const handleSuccess = () => {
    queryClient.invalidateQueries({ queryKey: ['labels', projectUrl] })
    onOpenChange(false)
    if (mode === 'edit') {
      onEditComplete()
    }
    setForm(initialFormState) // 폼 초기화
  }

  // --- 뮤테이션 정의 ---

  // 라벨 생성 (Create)
  const createMutation = useMutation({
    mutationFn: (payload: LabelCreateParams) => {
      if (!projectUrl) throw new Error('Project URL is required')
      return labelService.createLabel(projectUrl, payload)
    },
    onSuccess: handleSuccess,
    onError: error => {
      console.error('Failed to create label:', error)
      alert('라벨 생성에 실패했습니다.')
    },
  })

  // 라벨 수정 (Update)
  const updateMutation = useMutation({
    mutationFn: (payload: LabelUpdateParams) => {
      if (!projectUrl || !currentLabel)
        throw new Error('Context required for update')
      return labelService.updateLabel(projectUrl, currentLabel.id, payload)
    },
    onSuccess: handleSuccess,
    onError: error => {
      console.error('Failed to update label:', error)
      alert('라벨 수정에 실패했습니다.')
    },
  })

  // 라벨 삭제 (Delete)
  const deleteMutation = useMutation({
    mutationFn: (labelId: number) => {
      if (!projectUrl) throw new Error('Project URL is required')
      return labelService.deleteLabel(projectUrl, { labelId })
    },
    onSuccess: handleSuccess,
    onError: error => {
      console.error('Failed to delete label:', error)
      alert('라벨 삭제에 실패했습니다.')
    },
  })

  // --- 핸들러 함수 ---

  // 메인 제출 핸들러
  const handleSubmit = () => {
    if (!form.name.trim()) {
      alert('라벨 이름을 입력해주세요.')
      return
    }

    if (mode === 'create') {
      createMutation.mutate(form)
    } else if (mode === 'edit') {
      updateMutation.mutate(form)
    }
  }

  // 삭제 핸들러 (Edit 모드에서만 사용)
  const handleDelete = () => {
    if (!currentLabel) return
    if (confirm(`정말 '${currentLabel.name}' 라벨을 삭제하시겠습니까?`)) {
      deleteMutation.mutate(currentLabel.id)
    }
  }

  // 랜덤 색상 생성 (Edit 모드에서만 제공했던 기능)
  const generateRandomColor = () => {
    const randomColor = `#${Math.floor(Math.random() * 16777215)
      .toString(16)
      .padStart(6, '0')}`
    setForm({ ...form, color: randomColor })
  }

  // 색상 선택 기능 (Create 모드에서 제공했던 기능)
  const selectColor = () => {
    const color = prompt('색상 코드를 입력하세요 (예: #FF4040):', form.color)
    if (color && /^#[0-9A-F]{6}$/i.test(color)) {
      setForm({ ...form, color })
    } else if (color) {
      alert('올바른 색상 코드를 입력해주세요.')
    }
  }

  const isPending =
    createMutation.isPending ||
    updateMutation.isPending ||
    deleteMutation.isPending

  return (
    <Dialog open={isOpen} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-[523px]" showCloseButton={false}>
        <DialogHeader>
          <DialogTitle className="sr-only">{dialogTitle}</DialogTitle>
        </DialogHeader>
        <div className="flex flex-col gap-6">
          {/* Name Field */}
          <div className="flex flex-col gap-3">
            <label className="text-sm font-semibold text-[#6d758f]">Name</label>
            <Input
              placeholder="Label name"
              value={form.name}
              onChange={e => setForm({ ...form, name: e.target.value })}
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
                value={form.color}
                onChange={e => setForm({ ...form, color: e.target.value })}
                className="border-[#f1f3f7]"
                style={{ color: form.color }}
              />
            </div>
            {/* 모드에 따라 버튼 기능 변경 */}
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
              value={form.description}
              onChange={e => setForm({ ...form, description: e.target.value })}
              className="h-[92px] resize-none border-[#f1f3f7]"
            />
          </div>
        </div>

        <DialogFooter className="gap-6">
          {/* Delete 버튼은 'edit' 모드일 때만 표시 */}
          {mode === 'edit' && (
            <Button
              variant="destructive"
              onClick={handleDelete}
              disabled={isPending}
              className="bg-[#e86c6c] px-[18px] py-[14px] text-sm font-semibold text-white hover:bg-[#e86c6c]/90"
            >
              Delete
            </Button>
          )}

          {/* Cancel/Close 버튼 */}
          <Button
            variant="destructive"
            onClick={() => onOpenChange(false)}
            className={`${mode === 'create' ? 'bg-[#e86c6c]' : 'bg-gray-400'} px-[18px] py-[14px] text-sm font-semibold text-white ${mode === 'create' ? 'hover:bg-[#e86c6c]/90' : 'hover:bg-gray-400/90'}`}
          >
            {mode === 'create' ? 'Cancel' : 'Close'}
          </Button>

          {/* Submit 버튼 (Create 또는 Save) */}
          <Button
            onClick={handleSubmit}
            disabled={isPending}
            className="bg-[#6d758f] px-[18px] py-[14px] text-sm font-semibold text-white hover:bg-[#6d758f]/90"
          >
            {submitButtonText}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
