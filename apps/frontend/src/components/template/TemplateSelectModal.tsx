import { useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { FileText, AlertCircle } from 'lucide-react'
import { templateService } from '@/api/services/templateService'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Card } from '@/components/ui/card'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import type { TemplateListItem } from '@/types/template'

// Types
export type ResourceType = 'issues' | 'meetings' | 'retros' | 'scrums'

export type SelectedTemplate = {
  templateId: number | null // null for blank template
  title: string
  description: string
}

export type TemplateSelectModalProps = {
  isOpen: boolean
  onClose: () => void
  projectUrl: string | undefined
  resourceType: ResourceType
  onSelectTemplate: (template: SelectedTemplate) => void
}

// Resource labels mapping
const RESOURCE_LABELS: Record<ResourceType, string> = {
  issues: 'Issue',
  meetings: 'Meeting',
  retros: 'Retro',
  scrums: 'Scrum',
}

// Response keys mapping
const RESOURCE_RESPONSE_KEYS: Record<ResourceType, string> = {
  issues: 'issueTemplates',
  meetings: 'meetingTemplates',
  retros: 'retroTemplates',
  scrums: 'scrumTemplates',
}

export function TemplateSelectModal({
  isOpen,
  onClose,
  projectUrl,
  resourceType,
  onSelectTemplate,
}: TemplateSelectModalProps) {
  // Fetch templates
  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['templates', projectUrl, resourceType],
    queryFn: () => templateService.getTemplates(projectUrl, resourceType),
    enabled: !!projectUrl && isOpen,
  })

  // Extract templates from response
  const templates = useMemo((): TemplateListItem[] => {
    if (!data) return []

    const key = RESOURCE_RESPONSE_KEYS[resourceType]
    const responseData = data as Record<string, TemplateListItem[] | number>
    const templatesArray = responseData[key]
    return Array.isArray(templatesArray) ? templatesArray : []
  }, [data, resourceType])

  // Handlers
  const handleSelectBlank = () => {
    onSelectTemplate({
      templateId: null,
      title: '',
      description: '',
    })
    onClose()
  }

  const handleSelectTemplate = (template: TemplateListItem) => {
    onSelectTemplate({
      templateId: template.templateId,
      title: template.title,
      description: template.description,
    })
    onClose()
  }

  const resourceLabel = RESOURCE_LABELS[resourceType]

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-[800px] max-h-[80vh]" showCloseButton>
        <DialogHeader>
          <DialogTitle className="text-2xl font-bold">
            Choose a template
          </DialogTitle>
        </DialogHeader>

        <ScrollArea className="h-full max-h-[60vh] pr-4">
          {/* Loading State */}
          {isLoading && (
            <div className="grid grid-cols-1 gap-4 p-2 md:grid-cols-2">
              {Array.from({ length: 6 }).map((_, idx) => (
                <Skeleton key={idx} className="h-[120px] w-full rounded-lg" />
              ))}
            </div>
          )}

          {/* Error State */}
          {isError && (
            <div className="flex flex-col items-center justify-center gap-4 py-8">
              <div className="flex items-center gap-2 text-[#e86c6c]">
                <AlertCircle className="h-5 w-5" />
                <p className="text-sm font-medium">Failed to load templates</p>
              </div>
              <Button
                onClick={() => refetch()}
                className="bg-[#6d758f] px-4 py-2 text-sm font-semibold text-white hover:bg-[#6d758f]/90"
              >
                Retry
              </Button>
              <div className="mt-4 text-center text-sm text-[#6d758f]">
                Or continue with a blank {resourceLabel.toLowerCase()}:
              </div>
              <Card
                className="flex min-h-[120px] w-full cursor-pointer flex-col justify-center gap-2 border-2 border-dashed border-[#e1e4ed] p-6 transition-colors hover:bg-gray-50/50"
                onClick={handleSelectBlank}
              >
                <div className="flex items-center gap-2">
                  <FileText className="h-5 w-5 text-[#6d758f]" />
                  <h3 className="text-base font-semibold text-gray-900">
                    Blank {resourceLabel}
                  </h3>
                </div>
                <p className="text-sm text-[#6d758f]">Start from scratch</p>
              </Card>
            </div>
          )}

          {/* Templates Grid */}
          {!isLoading && !isError && (
            <div className="flex flex-col gap-2">
              {/* Blank Template Card */}
              <Card
                className="flex cursor-pointer flex-col justify-center gap-2 border-2 border-dashed border-[#e1e4ed] p-3 transition-colors hover:bg-gray-50/50"
                onClick={handleSelectBlank}
              >
                <div className="flex items-center gap-2">
                  <FileText className="h-5 w-5 text-[#6d758f]" />
                  <h3 className="text-base font-semibold text-gray-900">
                    Blank {resourceLabel}
                  </h3>
                </div>
                <p className="text-sm text-[#6d758f]">Start from scratch</p>
              </Card>

              {/* Template Cards */}
              {templates.map(template => (
                <Card
                  key={template.templateId}
                  className="flex cursor-pointer flex-col gap-2 border-[1.7px] border-[#e1e4ed] p-3 transition-colors hover:bg-gray-50"
                  onClick={() => handleSelectTemplate(template)}
                >
                  <h3 className="text-base font-semibold text-gray-900 line-clamp-2">
                    {template.title}
                  </h3>
                  <p className="text-sm text-[#6d758f] line-clamp-3">
                    {template.description}
                  </p>
                </Card>
              ))}

              {/* Empty State Message */}
              {templates.length === 0 && (
                <div className="col-span-2 py-4 text-center text-sm text-[#6d758f]">
                  No templates available. Start with a blank{' '}
                  {resourceLabel.toLowerCase()}.
                </div>
              )}
            </div>
          )}
        </ScrollArea>
      </DialogContent>
    </Dialog>
  )
}
