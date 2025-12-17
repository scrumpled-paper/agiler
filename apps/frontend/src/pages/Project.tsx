import KanbanView from '@/components/kanban/KanbanView'
import TableView from '@/components/table/TableView'
import ProjectSummaryCard from '@/components/ProjectSummaryCard'
import { Button } from '@/components/ui/button'
import { issueColumns } from '@/mocks/mockTasks'
import { Info, LayoutGrid, Table } from 'lucide-react'
import { useState, useMemo } from 'react'
import type { Issue } from '@/types'
import { useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { kanbanService } from '@/api/services/kanbanService'
import { KanbanDateSelector } from '@/components/kanban/KanbanDateSelector'
import { Alert, AlertDescription } from '@/components/ui/alert'

type ViewMode = 'kanban' | 'table'

export default function Project() {
  const { projectUrl } = useParams<{ projectUrl: string }>()
  const [viewMode, setViewMode] = useState<ViewMode>('kanban')
  const [selectedDate, setSelectedDate] = useState<string>(
    new Date().toISOString().split('T')[0]
  )
  const queryClient = useQueryClient()

  // Check if selected date is today
  const isToday = useMemo(() => {
    const today = new Date().toISOString().split('T')[0]
    return selectedDate === today
  }, [selectedDate])

  // MSW를 통해 칸반 데이터 조회
  const { data, isLoading, isError } = useQuery({
    queryKey: ['kanban', projectUrl, selectedDate],
    queryFn: () => kanbanService.getIssues(projectUrl!, selectedDate),
    enabled: !!projectUrl, // projectUrl이 있을 때만 쿼리 실행
  })

  // 드래그 앤 드롭 시 API 호출 (낙관적 업데이트 + 에러 롤백)
  const updateMutation = useMutation({
    mutationFn: (updatedTasks: Issue[]) =>
      kanbanService.updateIssue(projectUrl!, 'bulk', updatedTasks),

    onMutate: async (updatedTasks: Issue[]) => {
      // 진행 중인 쿼리 취소 (낙관적 업데이트와 충돌 방지)
      await queryClient.cancelQueries({ queryKey: ['kanban', projectUrl] })

      // 이전 데이터 백업 (에러 시 롤백용)
      const previousData = queryClient.getQueryData(['kanban', projectUrl])

      // 낙관적 업데이트
      queryClient.setQueryData(
        ['kanban', projectUrl],
        (oldData: { contents: Issue[]; size: number } | undefined) => ({
          contents: updatedTasks,
          size: updatedTasks.length,
          ...oldData,
        })
      )

      // 롤백을 위한 이전 데이터 반환
      return { previousData }
    },
    onError: (err, _updatedTasks, context) => {
      // 에러 발생 시 이전 데이터로 롤백
      if (context?.previousData) {
        queryClient.setQueryData(['kanban', projectUrl], context.previousData)
      }
      console.error('칸반 업데이트 실패:', err)
    },
    // onSuccess 제거 - 낙관적 업데이트만으로 충분
  })

  const handleTasksChange = (updatedTasks: Issue[]) => {
    // mutation 실행 (onMutate에서 낙관적 업데이트 처리)
    updateMutation.mutate(updatedTasks)
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

  const tasks: Issue[] = (data?.contents as Issue[]) || []

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
          columns={issueColumns}
          tasks={tasks}
          onTasksChange={handleTasksChange}
          isReadOnly={!isToday}
        />
      ) : (
        <TableView columns={issueColumns} tasks={tasks} />
      )}
    </div>
  )
}
