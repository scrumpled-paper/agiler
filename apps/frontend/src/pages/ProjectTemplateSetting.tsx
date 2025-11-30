import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Plus, MoreHorizontal } from 'lucide-react'
import { templateService } from '@/api/services/templateService'
import { TemplateFormModal } from '@/components/template/TemplateFormModal'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import type { TemplateListItem } from '@/types/template'

type ResourceType = 'issues' | 'meetings' | 'retros' | 'scrums'

const RESOURCE_LABELS: Record<ResourceType, string> = {
  issues: 'Issues',
  meetings: 'Meetings',
  retros: 'Retros',
  scrums: 'Scrums',
}

const RESOURCE_RESPONSE_KEYS: Record<ResourceType, string> = {
  issues: 'issueTemplates',
  meetings: 'meetingTemplates',
  retros: 'retroTemplates',
  scrums: 'scrumTemplates',
}

export default function ProjectTemplateSetting() {
  const { projectUrl } = useParams<{ projectUrl: string }>()

  // Selected resource type
  const [selectedResourceType, setSelectedResourceType] =
    useState<ResourceType>('issues')

  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [modalMode, setModalMode] = useState<'create' | 'edit'>('create')
  const [editingTemplate, setEditingTemplate] =
    useState<TemplateListItem | null>(null)

  // Query to fetch templates
  const { data, isLoading, isError } = useQuery({
    queryKey: ['templates', projectUrl, selectedResourceType],
    queryFn: () =>
      templateService.getTemplates(projectUrl!, selectedResourceType),
    enabled: !!projectUrl,
  })

  // Handlers
  const handleCreateClick = () => {
    setModalMode('create')
    setEditingTemplate(null)
    setIsModalOpen(true)
  }

  const handleEditClick = (template: TemplateListItem) => {
    setModalMode('edit')
    setEditingTemplate(template)
    setIsModalOpen(true)
  }

  const handleModalClose = () => {
    setIsModalOpen(false)
    setEditingTemplate(null)
  }

  // Extract templates from response
  const getTemplates = (): TemplateListItem[] => {
    if (!data) return []

    const key = RESOURCE_RESPONSE_KEYS[selectedResourceType]
    const responseData = data as Record<string, TemplateListItem[] | number>
    const templates = responseData[key]
    return Array.isArray(templates) ? templates : []
  }

  const templates = getTemplates()

  if (isLoading) {
    return (
      <div className="flex h-96 items-center justify-center">로딩 중...</div>
    )
  }

  if (isError) {
    return <div className="container p-4">에러가 발생했습니다.</div>
  }

  return (
    <div className="container mx-auto flex flex-col items-center gap-8 p-8">
      {/* Title */}
      <div className="flex w-full max-w-[1100px] flex-col items-center gap-6">
        <h1 className="text-center text-[40px] font-bold leading-[48px] text-black">
          Template Setting
        </h1>

        {/* Category Filter */}
        <div className="flex w-full items-center gap-4">
          <label className="text-sm font-semibold text-[#6d758f]">
            Category
          </label>
          <Select
            value={selectedResourceType}
            onValueChange={value =>
              setSelectedResourceType(value as ResourceType)
            }
          >
            <SelectTrigger className="w-[200px] border-[#e1e4ed]">
              <SelectValue />
            </SelectTrigger>
            <SelectContent className="bg-white">
              {(Object.keys(RESOURCE_LABELS) as ResourceType[]).map(type => (
                <SelectItem key={type} value={type}>
                  {RESOURCE_LABELS[type]}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* Template List */}
      <div className="flex w-full max-w-[1100px] flex-col gap-5 ">
        {templates.map(template => (
          <div
            key={template.templateId}
            className="flex min-h-[90px] items-center justify-between rounded-[10px] border-[1.7px] border-[#e1e4ed] bg-white px-9 py-5"
          >
            <div className="flex flex-col gap-1">
              <p className="text-[16px] font-semibold leading-[28px] text-black">
                {template.title}
              </p>
              <p className="text-sm text-[#6d758f]">{template.description}</p>
            </div>
            <button
              onClick={() => handleEditClick(template)}
              className="flex size-8 items-center justify-center transition-opacity hover:opacity-70"
            >
              <MoreHorizontal className="size-6 text-[#6d758f]" />
            </button>
          </div>
        ))}
        {/* Create New Template Button */}
        <button
          onClick={handleCreateClick}
          className="flex h-[90px] items-center justify-center rounded-[10px] border-[1.7px] border-[#e1e4ed] bg-white transition-colors hover:bg-gray-50"
        >
          <div className="flex size-[34px] rotate-45 items-center justify-center">
            <Plus className="size-6 rotate-[-45deg] text-[#6d758f]" />
          </div>
        </button>
      </div>

      {/* Unified Template Form Modal */}
      <TemplateFormModal
        mode={modalMode}
        isOpen={isModalOpen}
        onClose={handleModalClose}
        currentTemplate={editingTemplate}
        projectUrl={projectUrl}
        resourceType={selectedResourceType}
      />
    </div>
  )
}
