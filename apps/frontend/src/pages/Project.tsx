import KanbanView from '@/components/kanban/KanbanView'
import TableView from '@/components/table/TableView'
import ProjectSummaryCard from '@/components/ProjectSummaryCard'
import { Button } from '@/components/ui/button'
// import { issueColumns } from '@/mocks/mockTasks'
import { Info, LayoutGrid, Table } from 'lucide-react'
import { useState, useMemo } from 'react'
import {
  toIssueColumns,
  toIssues,
  type GetFilteredIssuesResponse,
  type Issue,
  type IssuePayload,
} from '@/types/issue'
import { useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { kanbanService } from '@/api/services/kanbanService'
import { KanbanDateSelector } from '@/components/kanban/KanbanDateSelector'
import { Alert, AlertDescription } from '@/components/ui/alert'
import {
  TemplateSelectModal,
  type SelectedTemplate,
} from '@/components/template/TemplateSelectModal'
import { issueService } from '@/api/services/issueService'
import { IssueModal } from '@/components/IssueModal'
import type { IssueColumn } from '@/types'
import type { UserInfo } from '../types/index'
import type { Label } from '@/types/label'

type ViewMode = 'kanban' | 'table'

interface KanbanData {
  tasks: Issue[]
  columns: IssueColumn[]
  profiles: UserInfo[]
  labels: Label[]
}

export default function Project() {
  const { projectUrl } = useParams<{ projectUrl: string }>()
  const [viewMode, setViewMode] = useState<ViewMode>('kanban')
  const [issueModalOpen, setIssueModalOpen] = useState<boolean>(false)
  const [selectedDate, setSelectedDate] = useState<string>(
    new Date().toISOString().split('T')[0]
  )
  const [templateModalOpen, setTemplateModalOpen] = useState(false)
  const [selectedTemplate, setSelectedTemplate] =
    useState<SelectedTemplate | null>(null)

  // Edit modal state
  const [editIssueId, setEditIssueId] = useState<number | null>(null)
  const [editModalOpen, setEditModalOpen] = useState<boolean>(false)

  const queryClient = useQueryClient()
  // Check if selected date is today
  const isToday = useMemo(() => {
    const today = new Date().toISOString().split('T')[0]
    return selectedDate === today
  }, [selectedDate])

  const { data, isLoading, isError } = useQuery<
    GetFilteredIssuesResponse,
    Error,
    KanbanData
  >({
    queryKey: ['kanban', projectUrl, selectedDate],
    queryFn: () =>
      kanbanService.getFilteredIssues(projectUrl!, { date: selectedDate }),
    enabled: !!projectUrl,
    select: (response): KanbanData => ({
      tasks: toIssues(response.issues),
      columns: toIssueColumns(response.kanbanConfigs),
      profiles: response.profiles,
      labels: response.labels,
    }),
  })

  // 드래그 앤 드롭 시 개별 이슈 상태 업데이트
  const updateIssueStatusMutation = useMutation({
    mutationFn: ({
      issueId,
      kanbanConfigId,
    }: {
      issueId: number
      kanbanConfigId: number
    }) => {
      return issueService.updateIssueStatus(
        projectUrl!,
        issueId,
        kanbanConfigId
      )
    },

    onSuccess: () => {
      // 성공 시 쿼리 무효화하여 최신 데이터 자동 refetch
      queryClient.invalidateQueries({
        queryKey: ['kanban', projectUrl, selectedDate],
      })
    },
    onError: err => {
      console.error('❌ API Error:', err)
    },
  })

  const handleTaskStatusChange = (issueId: number, kanbanConfigId: number) => {
    // 개별 이슈 상태 업데이트 API 호출
    updateIssueStatusMutation.mutate({ issueId, kanbanConfigId })
  }

  const handleTemplateSelect = (template: SelectedTemplate) => {
    // template.templateId will be null for blank template
    // Use template data to open IssueModal with pre-filled content
    setSelectedTemplate(template)
    setIssueModalOpen(true)
  }

  const handleIssueSave = async (issueData: IssuePayload) => {
    try {
      await issueService.createIssue(projectUrl, issueData)
    } catch (error) {
      console.error(error)
    } finally {
      // After successful save, refetch the kanban data
      queryClient.invalidateQueries({ queryKey: ['kanban', projectUrl] })
    }
  }

  const handleCardClick = (issueId: number) => {
    setEditIssueId(issueId)
    setEditModalOpen(true)
  }

  const handleEditModalClose = () => {
    setEditModalOpen(false)
    setEditIssueId(null)
    // Refetch kanban data after editing
    queryClient.invalidateQueries({ queryKey: ['kanban', projectUrl] })
  }

  if (isLoading) {
    return (
      <div className="container p-4 ">
        <div className="bg-card rounded-lg border shadow-sm p-6"></div>
        <div className="bg-card rounded-lg border shadow-sm p-6">
          <div className="h-8 bg-gray-200 rounded animate-pulse mb-4"></div>
          <div className="flex gap-6">
            <div className="w-1/2 h-64 bg-gray-200 rounded animate-pulse"></div>
            <div className="w-1/2 space-y-2">
              <div className="h-4 bg-gray-200 rounded animate-pulse"></div>
              <div className="h-4 bg-gray-200 rounded animate-pulse w-5/6"></div>
              <div className="h-4 bg-gray-200 rounded animate-pulse w-4/6"></div>
            </div>
          </div>
        </div>
      </div>
    )
  }

  // const tasks: Issue[] = toIssues(data) || []
  const { tasks = [], columns = [], labels = [], profiles = [] } = data || {}
  return (
    <div className="container p-4">
      <ProjectSummaryCard />

      {isError && (
        <Alert className="flex flex-row gap-2 items-center my-5">
          <Info className="h-4 w-4" />
          <AlertDescription>데이터가 없습니다</AlertDescription>
        </Alert>
      )}
      {/* 뷰 전환 버튼 및 날짜 선택기 */}
      <div className="flex items-center justify-between my-4">
        <div className="flex items-center gap-2">
          <Button
            variant={viewMode === 'kanban' ? 'default' : 'outline'}
            size="sm"
            onClick={() => setViewMode('kanban')}
            className="gap-2"
          >
            <LayoutGrid className="h-4 w-4" />
            Kanban
          </Button>
          <Button
            variant={viewMode === 'table' ? 'default' : 'outline'}
            size="sm"
            onClick={() => setViewMode('table')}
            className="gap-2"
          >
            <Table className="h-4 w-4" />
            Table
          </Button>
        </div>

        <KanbanDateSelector
          selectedDate={selectedDate}
          onDateChange={setSelectedDate}
        />
      </div>

      {/* 조건부 렌더링: viewMode에 따라 다른 뷰 표시 */}
      {viewMode === 'kanban' ? (
        <KanbanView
          // columns={issueColumns}
          columns={columns}
          tasks={tasks}
          onTaskStatusChange={handleTaskStatusChange}
          onCardClick={handleCardClick}
          isReadOnly={!isToday}
          labels={labels}
          profiles={profiles}
        />
      ) : (
        // <TableView columns={issueColumns} tasks={tasks} />
        <TableView columns={columns} tasks={tasks} profiles={profiles} />
      )}
      <Button
        variant={viewMode === 'kanban' ? 'default' : 'outline'}
        size="sm"
        onClick={() => {
          setTemplateModalOpen(true)
        }}
        className="gap-2"
      >
        <LayoutGrid className="h-4 w-4" />
        이슈 생성하기
      </Button>
      {templateModalOpen && (
        <TemplateSelectModal
          isOpen={templateModalOpen}
          onClose={() => setTemplateModalOpen(false)}
          projectUrl={projectUrl}
          resourceType="issues"
          onSelectTemplate={handleTemplateSelect}
        />
      )}
      {issueModalOpen && (
        <IssueModal
          isOpen={issueModalOpen}
          onClose={() => {
            setIssueModalOpen(false)
            setSelectedTemplate(null)
          }}
          projectUrl={projectUrl}
          selectedTemplate={selectedTemplate}
          onSave={handleIssueSave}
        />
      )}

      {/* Edit Issue Modal */}
      {editModalOpen && (
        <IssueModal
          mode="edit"
          issueId={editIssueId ?? undefined}
          isOpen={editModalOpen}
          onClose={handleEditModalClose}
          projectUrl={projectUrl}
        />
      )}
    </div>
  )
}
