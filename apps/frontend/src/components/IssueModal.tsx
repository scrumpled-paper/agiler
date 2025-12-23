import { useState, useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { ScrollArea } from '@/components/ui/scroll-area'
import { projectService } from '@/api/services/projectService'
import { labelService } from '@/api/services/labelService'
import type { UserInfo } from '@/types'
import type { Label } from '@/types/label'
import type { SelectedTemplate } from './template/TemplateSelectModal'
import { AssigneeSelector } from './issue/AssigneeSelector'
import { SelectedAssignees } from './issue/SelectedAssignees'
import { TimeSelector } from './issue/TimeSelector'
import { LabelSelector } from './issue/LabelSelector'
import { TemplateContentEditor } from './issue/TemplateContentEditor'
import { templateService } from '@/api/services/templateService'
import type { IssuePayload } from '@/types/issue'

export type IssueModalProps = {
  isOpen: boolean
  onClose: () => void
  projectUrl: string | undefined
  selectedTemplate?: SelectedTemplate | null
  onSave?: (issueData: IssuePayload) => void
}

export function IssueModal({
  isOpen,
  onClose,
  projectUrl,
  selectedTemplate,
  onSave,
}: IssueModalProps) {
  // Form state
  const [title, setTitle] = useState('')
  const [contents, setContents] = useState('')
  const [startedAt, setStartedAt] = useState('')
  const [dueAt, setDueAt] = useState('')
  const [selectedAssignees, setSelectedAssignees] = useState<UserInfo[]>([])
  const [selectedLabels, setSelectedLabels] = useState<Label[]>([])

  // Popover states
  const [assigneePopoverOpen, setAssigneePopoverOpen] = useState(false)
  const [labelPopoverOpen, setLabelPopoverOpen] = useState(false)

  // Fetch project members
  const { data: membersData } = useQuery({
    queryKey: ['project-members', projectUrl],
    queryFn: () =>
      projectService.getProjectMember({
        projectUrl: projectUrl!,
        size: 100,
        page: 0,
      }),
    enabled: !!projectUrl && isOpen,
  })

  // Fetch labels
  const { data: labelsData } = useQuery({
    queryKey: ['labels', projectUrl],
    queryFn: () => labelService.getLabels(projectUrl!),
    enabled: !!projectUrl && isOpen,
  })

  // Initialize form with template data
  useEffect(() => {
    if (selectedTemplate) {
      // setTitle(selectedTemplate.title)
      // setContents(selectedTemplate.description)
      const handleContent = async () => {
        if (projectUrl && selectedTemplate.templateId) {
          try {
            templateService
              .getTemplateDetail(
                projectUrl,
                'issues',
                selectedTemplate.templateId
              )
              .then(detail => {
                setContents(detail.contents)
              })
          } catch {
            console.error('템플릿 불러오기 실패')
          }
        }
      }
      handleContent()
    }
  }, [])

  // Reset form when modal closes
  useEffect(() => {
    if (!isOpen) {
      setTitle('')
      setContents('')
      setStartedAt('')
      setDueAt('')
      setSelectedAssignees([])
      setSelectedLabels([])
    }
  }, [isOpen])

  // Handlers
  const handleAddAssignee = (member: UserInfo) => {
    if (!selectedAssignees.find(a => a.profileId === member.profileId)) {
      setSelectedAssignees([...selectedAssignees, member])
    }
    setAssigneePopoverOpen(false)
  }

  const handleRemoveAssignee = (profileId: number) => {
    setSelectedAssignees(
      selectedAssignees.filter(a => a.profileId !== profileId)
    )
  }

  const handleAddLabel = (label: Label) => {
    if (!selectedLabels.find(l => l === label)) {
      setSelectedLabels([...selectedLabels, label])
    }
    setLabelPopoverOpen(false)
  }

  const handleRemoveLabel = (label: Label) => {
    setSelectedLabels(selectedLabels.filter(l => l !== label))
  }

  const handleSave = () => {
    const issueData: IssuePayload = {
      title,
      contents,
      startedAt,
      dueAt,
      assignees: selectedAssignees.map(p => p.profileId),
      labels: selectedLabels.map(l => l.labelId),
    }
    onSave?.(issueData)
    onClose()
  }

  const handleCancel = () => {
    onClose()
  }

  const members = membersData?.contents || []
  const labels = labelsData?.labels || []

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-h-[90vh] overflow-hidden p-0">
        <ScrollArea className="h-full max-h-[90vh]">
          <div className="flex flex-col gap-5 bg-[#f8faff] px-8 py-12">
            {/* Header */}
            <DialogHeader className="sr-only">
              <DialogTitle>Create Issue</DialogTitle>
            </DialogHeader>

            {/* Title Input */}
            <div className="flex flex-col">
              <label className="text-sm font-bold text-black">제목</label>
              <Input
                placeholder="Issue Title"
                value={title}
                onChange={e => setTitle(e.target.value)}
                className="w-full rounded-[5px] border-[0.67px] border-[#e1e4ed] bg-white px-4 text-center text-[40px] font-semibold text-[#6d758f] shadow-sm"
              />
            </div>

            {/* Assignees */}
            <div className="flex flex-col gap-1">
              <label className="text-sm font-bold text-black">담당자</label>
              <div className="flex flex-wrap items-center gap-2">
                <AssigneeSelector
                  members={members}
                  onAdd={handleAddAssignee}
                  isOpen={assigneePopoverOpen}
                  onOpenChange={setAssigneePopoverOpen}
                />
                <SelectedAssignees
                  assignees={selectedAssignees}
                  onRemove={handleRemoveAssignee}
                />
              </div>
            </div>
            {/* Start and Due Time */}
            <TimeSelector
              startedAt={startedAt}
              dueAt={dueAt}
              setStartedAt={setStartedAt}
              setDueAt={setDueAt}
            />

            {/* Labels */}
            <LabelSelector
              labels={labels}
              selectedLabels={selectedLabels}
              onAdd={handleAddLabel}
              onRemove={handleRemoveLabel}
              isOpen={labelPopoverOpen}
              onOpenChange={setLabelPopoverOpen}
            />

            {/* Template Content */}
            <TemplateContentEditor value={contents} onChange={setContents} />

            {/* Action Buttons */}
            <div className="flex gap-3">
              <Button
                type="button"
                variant="outline"
                onClick={handleCancel}
                className="h-12 flex-1 rounded-lg border-black text-base font-medium text-black hover:bg-gray-50"
              >
                Cancel
              </Button>
              <Button
                type="button"
                onClick={handleSave}
                className="h-12 flex-1 rounded-lg bg-black text-base font-medium text-white hover:bg-black/90"
              >
                Save
              </Button>
            </div>
          </div>
        </ScrollArea>
      </DialogContent>
    </Dialog>
  )
}
