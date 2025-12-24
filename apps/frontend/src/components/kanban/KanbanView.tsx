import { useMemo, useState } from 'react'
import type { DragEndEvent } from '@/components/ui/shadcn-io/kanban'
import {
  KanbanBoard,
  KanbanCard,
  KanbanCards,
  KanbanHeader,
  KanbanProvider,
} from '@/components/ui/shadcn-io/kanban'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Info } from 'lucide-react'
import type { IssueColumn, UserInfo } from '@/types'
import {
  KanbanFilterBar,
  type KanbanFilters,
} from '@/components/kanban/KanbanFilterBar'
import type { Issue } from '@/types/issue'
import type { Label } from '@/types/label'

interface KanbanViewProps {
  columns: IssueColumn[]
  tasks: Issue[]
  onTaskStatusChange?: (issueId: number, kanbanConfigId: number) => void
  onCardClick?: (issueId: number) => void
  isReadOnly?: boolean
  labels: Label[]
  profiles: UserInfo[]
}

export default function KanbanView({
  columns,
  tasks,
  onTaskStatusChange,
  onCardClick,
  isReadOnly = false,
  labels,
  profiles,
}: KanbanViewProps) {
  const [filters, setFilters] = useState<KanbanFilters>({
    search: '',
    sortBy: 'endAt-asc', // 내부 값은 유지하되 로직에서 dueAt 연결
    selectedOwners: [],
    selectedLabels: [],
    selectedSubscribers: [],
  })

  // 1. 빠른 조회를 위해 labels 배열을 Map으로 변환
  const labelMap = useMemo(
    () => new Map(labels.map(l => [Number(l.labelId), l])),
    [labels]
  )

  const profileMap = useMemo(
    () => new Map(profiles.map(p => [Number(p.profileId), p])),
    [profiles]
  )

  // 2. 담당자, 라벨, 구독자 추출 로직
  const { availableOwners, availableLabels, availableSubscribers } =
    useMemo(() => {
      const ownersMap = new Map<number, UserInfo>()
      const labelsMap = new Map<number, Label>()
      const subscribersMap = new Map<number, UserInfo>()

      tasks.forEach(task => {
        // 라벨 매핑
        task.labels?.forEach(labelId => {
          const labelDetail = labelMap.get(Number(labelId))
          if (labelDetail) labelsMap.set(labelDetail.labelId, labelDetail)
        })

        //  담당자(Assignees) 매핑
        task.assignees?.forEach(profileId => {
          const profileDetail = profileMap.get(Number(profileId))
          if (profileDetail?.profileId) {
            ownersMap.set(profileDetail.profileId, profileDetail)
          }
        })

        // 구독자(Notis) 매핑
        task.notis?.forEach(profileId => {
          const profileDetail = profileMap.get(Number(profileId))
          if (profileDetail?.profileId) {
            subscribersMap.set(profileDetail.profileId, profileDetail)
          }
        })
      })

      return {
        availableOwners: Array.from(ownersMap.values()),
        availableLabels: Array.from(labelsMap.values()),
        availableSubscribers: Array.from(subscribersMap.values()),
      }
      // labelMap과 profileMap이 변경될 때도 값이 갱신되도록 의존성 추가
    }, [tasks, labelMap, profileMap])

  const filteredAndSortedTasks = useMemo(() => {
    let result = [...tasks]

    // 검색 (name -> title)
    if (filters.search) {
      const searchLower = filters.search.toLowerCase()
      result = result.filter(task =>
        task.title.toLowerCase().includes(searchLower)
      )
    }

    // 담당자 필터 (assignees 중 한 명이라도 포함되는지 확인)
    if (filters.selectedOwners.length > 0) {
      result = result.filter(task =>
        task.assignees.some(a => filters.selectedOwners.includes(a))
      )
    }

    // 라벨 필터
    if (filters.selectedLabels.length > 0) {
      result = result.filter(task =>
        task.labels?.some(label => filters.selectedLabels.includes(label))
      )
    }

    // 구독자 필터 (notis)
    if (filters.selectedSubscribers.length > 0) {
      result = result.filter(task =>
        task.notis?.some(noti => filters.selectedSubscribers.includes(noti))
      )
    }

    // 정렬 (date 필드 변경 대응)
    result.sort((a, b) => {
      switch (filters.sortBy) {
        case 'endAt-asc':
          return new Date(a.dueAt).getTime() - new Date(b.dueAt).getTime()
        case 'endAt-desc':
          return new Date(b.dueAt).getTime() - new Date(a.dueAt).getTime()
        case 'startAt-asc':
          return (
            new Date(a.startedAt).getTime() - new Date(b.startedAt).getTime()
          )
        case 'startAt-desc':
          return (
            new Date(b.startedAt).getTime() - new Date(a.startedAt).getTime()
          )
        case 'name-asc':
          return a.title.localeCompare(b.title)
        default:
          return 0
      }
    })

    return result
  }, [tasks, filters])

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event
    if (!over) return

    const issueId = String(active.id)

    // over.id는 컬럼 id일 수도 있고, 다른 카드의 id일 수도 있음
    // 1. 먼저 컬럼에서 찾기
    // 2. 없으면 카드에서 찾아서 해당 카드의 kanbanConfigId 사용
    const overColumn =
      columns.find(column => column.id === over.id)?.id ??
      tasks.find(task => task.issueId === String(over.id))?.kanbanConfigId

    if (!overColumn) return

    const newKanbanConfigId = Number(overColumn)

    // 상태가 실제로 변경된 경우만 API 호출
    const targetTask = tasks.find(task => task.issueId === issueId)

    if (targetTask && targetTask.kanbanConfigId !== newKanbanConfigId) {
      // 개별 이슈 상태 업데이트 API 호출
      onTaskStatusChange?.(Number(issueId), newKanbanConfigId)
    }
  }

  const handleCardClick = (issueId: string) => {
    onCardClick?.(Number(issueId))
  }

  return (
    <div className="flex flex-col gap-4">
      {isReadOnly && (
        <Alert className="flex flex-row gap-2 items-center">
          <Info className="h-4 w-4" />
          <AlertDescription>
            과거 날짜의 칸반은 수정할 수 없습니다.
          </AlertDescription>
        </Alert>
      )}

      <KanbanFilterBar
        filters={filters}
        onFiltersChange={setFilters}
        availableOwners={availableOwners}
        availableLabels={availableLabels}
        availableSubscribers={availableSubscribers}
      />

      <KanbanProvider<Issue, IssueColumn>
        columns={columns}
        data={filteredAndSortedTasks}
        onDragEnd={handleDragEnd}
        isReadOnly={isReadOnly}
      >
        {column => (
          <KanbanBoard id={column.id} key={column.id}>
            <KanbanHeader>{column.name}</KanbanHeader>
            <KanbanCards<Issue> id={column.id}>
              {task => (
                <KanbanCard
                  column={String(task.kanbanConfigId)} // ID 매칭용
                  id={task.issueId}
                  key={task.issueId}
                  name={task.title}
                  onClick={() => handleCardClick(task.issueId)}
                >
                  <div className="flex flex-col gap-2">
                    <div className=" flex flex-row">
                      <p className="m-0 font-medium text-sm ">{task.title}</p>
                      {/* Notis (구독자) 렌더링 */}
                      {task.notis && task.notis.length > 0 && (
                        <div className="flex flex-1 items-start justify-end gap-1 ">
                          <div className="flex -space-x-2">
                            {task.notis.slice(0, 3).map((profileId, idx) => {
                              const subscriber = profileMap.get(profileId)
                              if (!subscriber) return null
                              return (
                                <Avatar
                                  key={idx}
                                  className="h-6 w-6 border-2 border-background"
                                >
                                  <AvatarImage
                                    src={subscriber.imageUrl}
                                    alt={subscriber.nickname}
                                  />
                                  <AvatarFallback className="text-[10px]">
                                    {subscriber.nickname
                                      .slice(0, 2)
                                      .toUpperCase()}
                                  </AvatarFallback>
                                </Avatar>
                              )
                            })}
                            {task.notis.length > 3 && (
                              <div className="flex h-6 w-6 items-center justify-center rounded-full border-2 border-background bg-muted text-[10px] font-medium">
                                +{task.notis.length - 3}
                              </div>
                            )}
                          </div>
                        </div>
                      )}
                    </div>

                    {/* Labels */}
                    {task.labels && task.labels.length > 0 && (
                      <div className="flex flex-wrap gap-1">
                        {task.labels.map(labelId => {
                          const label = labelMap.get(Number(labelId))
                          if (!label) return null // 상세 정보가 없으면 렌더링 안함
                          return (
                            <Badge
                              key={label.name}
                              variant="outline"
                              style={{
                                backgroundColor: label.color,
                                borderColor: label.color,
                                color: '#fff',
                              }}
                              className="text-[10px] px-1.5 py-0"
                            >
                              {label.name}
                            </Badge>
                          )
                        })}
                      </div>
                    )}

                    {/* Assignees (첫 번째 담당자 표시) 및 마감일 */}
                    <div className="flex items-center justify-between text-xs text-muted-foreground">
                      <div className="flex flex-row items-center gap-1">
                        {task.assignees &&
                          task.assignees.length > 0 &&
                          (() => {
                            const firstAssigneeId = task.assignees[0]
                            const firstAssignee =
                              profileMap.get(firstAssigneeId)
                            if (!firstAssignee) return null
                            return (
                              <>
                                <Avatar className="h-6 w-6 border-2">
                                  <AvatarImage src={firstAssignee.imageUrl} />
                                  <AvatarFallback>
                                    {firstAssignee.nickname
                                      .slice(0, 2)
                                      .toUpperCase()}
                                  </AvatarFallback>
                                </Avatar>
                                <span>
                                  {firstAssignee.nickname}
                                  {task.assignees.length > 1 &&
                                    ` 외 ${task.assignees.length - 1}명`}
                                </span>
                              </>
                            )
                          })()}
                      </div>
                      <span>
                        {new Date(task.dueAt).toLocaleDateString('ko-KR', {
                          month: 'short',
                          day: 'numeric',
                        })}
                      </span>
                    </div>
                  </div>
                </KanbanCard>
              )}
            </KanbanCards>
          </KanbanBoard>
        )}
      </KanbanProvider>
    </div>
  )
}
