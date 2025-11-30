import { useState, useEffect } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { templateService } from '@/api/services/templateService'
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
import MDEditor from '@uiw/react-md-editor'
import type {
  TemplateListItem,
  TemplateCreatePayload,
  TemplateUpdatePayload,
  TemplateDeletePayload,
} from '@/types/template'

type ResourceType = 'issues' | 'meetings' | 'retros' | 'scrums'

type TemplateFormData = {
  title: string
  description: string
  contents: string
}

type TemplateFormModalProps = {
  mode: 'create' | 'edit'
  isOpen: boolean
  onClose: () => void
  currentTemplate: TemplateListItem | null
  projectUrl: string | undefined
  resourceType: ResourceType
}

export function TemplateFormModal({
  mode,
  isOpen,
  onClose,
  currentTemplate,
  projectUrl,
  resourceType,
}: TemplateFormModalProps) {
  const queryClient = useQueryClient()

  // Form state
  const [formData, setFormData] = useState<TemplateFormData>({
    title: '',
    description: '',
    contents: '',
  })

  // Reset form when modal opens or mode/currentTemplate changes
  useEffect(() => {
    if (isOpen) {
      if (mode === 'edit' && currentTemplate) {
        // Fetch template detail to get the contents
        if (projectUrl) {
          templateService
            .getTemplateDetail(
              projectUrl,
              resourceType,
              currentTemplate.templateId
            )
            .then(detail => {
              setFormData({
                title: detail.title,
                description: detail.description,
                contents: detail.contents,
              })
            })
            .catch(error => {
              console.error('Failed to fetch template detail:', error)
              alert('템플릿 정보를 불러오는데 실패했습니다.')
            })
        }
      } else {
        setFormData({
          title: '',
          description: '',
          contents: '',
        })
      }
    }
  }, [isOpen, mode, currentTemplate, projectUrl, resourceType])

  // Create mutation
  const createMutation = useMutation({
    mutationFn: (payload: TemplateCreatePayload) => {
      if (!projectUrl) throw new Error('Project URL is required')
      return templateService.createTemplate(projectUrl, resourceType, payload)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['templates', projectUrl, resourceType],
      })
      onClose()
    },
    onError: error => {
      console.error('Failed to create template:', error)
      alert('템플릿 생성에 실패했습니다.')
    },
  })

  // Update mutation
  const updateMutation = useMutation({
    mutationFn: (payload: TemplateUpdatePayload) => {
      if (!projectUrl) throw new Error('Project URL is required')
      return templateService.updateTemplate(projectUrl, resourceType, payload)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['templates', projectUrl, resourceType],
      })
      onClose()
    },
    onError: error => {
      console.error('Failed to update template:', error)
      alert('템플릿 수정에 실패했습니다.')
    },
  })

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: (payload: TemplateDeletePayload) => {
      if (!projectUrl) throw new Error('Project URL is required')
      return templateService.deleteTemplate(projectUrl, resourceType, payload)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['templates', projectUrl, resourceType],
      })
      onClose()
    },
    onError: error => {
      console.error('Failed to delete template:', error)
      alert('템플릿 삭제에 실패했습니다.')
    },
  })

  // Handlers
  const handleSubmit = () => {
    if (!formData.title.trim()) {
      alert('템플릿 제목을 입력해주세요.')
      return
    }

    if (mode === 'create') {
      createMutation.mutate(formData)
    } else if (currentTemplate) {
      updateMutation.mutate({
        ...formData,
        templateId: currentTemplate.templateId,
      })
    }
  }

  const handleDelete = () => {
    if (!currentTemplate) return
    if (confirm(`정말 '${currentTemplate.title}' 템플릿을 삭제하시겠습니까?`)) {
      deleteMutation.mutate({ templateId: currentTemplate.templateId })
    }
  }

  const isPending =
    createMutation.isPending ||
    updateMutation.isPending ||
    deleteMutation.isPending

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent
        className="max-w-[1200px] sm:max-w-[900px] max-h-[100vh] overflow-y-auto"
        showCloseButton={false}
      >
        <DialogHeader>
          <DialogTitle className="sr-only">
            {mode === 'create' ? 'Create Template' : 'Edit Template'}
          </DialogTitle>
        </DialogHeader>

        <div className="flex flex-col gap-6">
          {/* Title Field */}
          <div className="flex flex-col gap-3">
            <label className="text-sm font-semibold text-[#6d758f]">
              Title
            </label>
            <Input
              placeholder="Template title"
              value={formData.title}
              onChange={e =>
                setFormData({ ...formData, title: e.target.value })
              }
              className="border-[#f1f3f7]"
            />
          </div>

          {/* Description Field */}
          <div className="flex flex-col gap-3">
            <label className="text-sm font-semibold text-[#6d758f]">
              Description
            </label>
            <Textarea
              placeholder="Template description"
              value={formData.description}
              onChange={e =>
                setFormData({ ...formData, description: e.target.value })
              }
              className="h-[92px] resize-none border-[#f1f3f7]"
            />
          </div>

          {/* Contents Field (Markdown Editor) */}
          <div className="flex flex-col gap-3">
            <label className="text-sm font-semibold text-[#6d758f]">
              Contents (Markdown)
            </label>
            <div data-color-mode="light">
              <MDEditor
                value={formData.contents}
                onChange={value =>
                  setFormData({ ...formData, contents: value || '' })
                }
                preview="edit"
                height={400}
              />
            </div>
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
