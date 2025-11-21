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
import type { Issue, IssueColumn, Label, UserInfo } from '@/types'
import {
  KanbanFilterBar,
  type KanbanFilters,
} from '@/components/kanban/KanbanFilterBar'

interface KanbanViewProps {
  columns: IssueColumn[]
  tasks: Issue[]
  onTasksChange: (tasks: Issue[]) => void
}

export default function KanbanView({
  columns,
  tasks,
  onTasksChange,
}: KanbanViewProps) {
  const [filters, setFilters] = useState<KanbanFilters>({
    search: '',
    sortBy: 'endAt-asc',
    selectedOwners: [],
    selectedLabels: [],
    selectedSubscribers: [],
  })

  // Extract unique owners, labels, and subscribers from all tasks
  const { availableOwners, availableLabels, availableSubscribers } =
    useMemo(() => {
      const ownersMap = new Map<string, UserInfo>()
      const labelsMap = new Map<string, Label>()
      const subscribersMap = new Map<string, UserInfo>()

      tasks.forEach(task => {
        // Add owner
        ownersMap.set(task.owner.nickname, task.owner)

        // Add labels
        task.labels?.forEach(label => {
          labelsMap.set(label.name, label)
        })

        // Add subscribers
        task.subscribers?.forEach(subscriber => {
          subscribersMap.set(subscriber.nickname, subscriber)
        })
      })

      return {
        availableOwners: Array.from(ownersMap.values()),
        availableLabels: Array.from(labelsMap.values()),
        availableSubscribers: Array.from(subscribersMap.values()),
      }
    }, [tasks])

  // Filter and sort tasks
  const filteredAndSortedTasks = useMemo(() => {
    let result = [...tasks]

    // Apply search filter
    if (filters.search) {
      const searchLower = filters.search.toLowerCase()
      result = result.filter(task =>
        task.name.toLowerCase().includes(searchLower)
      )
    }

    // Apply owner filter
    if (filters.selectedOwners.length > 0) {
      result = result.filter(task =>
        filters.selectedOwners.includes(task.owner.nickname)
      )
    }

    // Apply label filter
    if (filters.selectedLabels.length > 0) {
      result = result.filter(task =>
        task.labels?.some(label => filters.selectedLabels.includes(label.name))
      )
    }

    // Apply subscriber filter
    if (filters.selectedSubscribers.length > 0) {
      result = result.filter(task =>
        task.subscribers?.some(subscriber =>
          filters.selectedSubscribers.includes(subscriber.nickname)
        )
      )
    }

    // Apply sorting
    result.sort((a, b) => {
      switch (filters.sortBy) {
        case 'endAt-asc':
          return new Date(a.endAt).getTime() - new Date(b.endAt).getTime()
        case 'endAt-desc':
          return new Date(b.endAt).getTime() - new Date(a.endAt).getTime()
        case 'startAt-asc':
          return new Date(a.startAt).getTime() - new Date(b.startAt).getTime()
        case 'startAt-desc':
          return new Date(b.startAt).getTime() - new Date(a.startAt).getTime()
        case 'name-asc':
          return a.name.localeCompare(b.name)
        default:
          return 0
      }
    })

    return result
  }, [tasks, filters])

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event
    if (!over) {
      return
    }

    // active.id는 Task(Issue) id, over.id는 Column(IssueColumn) id
    const status = columns.find(({ id }) => id === over.id)
    if (!status) {
      return
    }

    // 컬럼 변경 로직
    const updatedTasks = tasks.map(task => {
      if (task.id === active.id) {
        // ⚠️ task의 'column' 속성 업데이트
        return { ...task, column: status.id }
      }
      return task
    })

    onTasksChange(updatedTasks)
  }

  return (
    <div className="flex flex-col gap-4">
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
        onDataChange={onTasksChange}
      >
        {column => (
          <KanbanBoard id={column.id} key={column.id}>
            <KanbanHeader>{column.name}</KanbanHeader>
            <KanbanCards<Issue> id={column.id}>
              {task => (
                <KanbanCard
                  column={task.column}
                  id={task.id}
                  key={task.id}
                  name={task.name}
                >
                  <div className="flex flex-col gap-2">
                    <div className=" flex flex-row">
                      {/* Task Name */}
                      <p className="m-0 font-medium text-sm ">{task.name}</p>
                      {/* Subscribers */}
                      {task.subscribers && task.subscribers.length > 0 && (
                        <div className="flex flex-1 items-start justify-end gap-1 ">
                          <div className="flex -space-x-2">
                            {task.subscribers
                              .slice(0, 3)
                              .map((subscriber, idx) => (
                                <Avatar
                                  key={idx}
                                  className="h-6 w-6 border-2 border-background"
                                >
                                  <AvatarImage
                                    src={subscriber.image}
                                    alt={subscriber.nickname}
                                  />
                                  <AvatarFallback className="text-[10px]">
                                    {subscriber.nickname
                                      .slice(0, 2)
                                      .toUpperCase()}
                                  </AvatarFallback>
                                </Avatar>
                              ))}
                            {task.subscribers.length > 3 && (
                              <div className="flex h-6 w-6 items-center justify-center rounded-full border-2 border-background bg-muted text-[10px] font-medium">
                                +{task.subscribers.length - 3}
                              </div>
                            )}
                          </div>
                        </div>
                      )}
                    </div>

                    {/* Owner and Date */}
                    <div className="flex items-center justify-between text-xs text-muted-foreground">
                      {/* task.owner는 Issue['owner'] (UserInfo) 타입입니다. */}
                      <div className="flex flex-ro items-center gap-1">
                        <Avatar className="h-6 w-6 border-2">
                          <AvatarImage src={task.owner.image} />
                          <AvatarFallback>
                            {task.owner.nickname.slice(0, 2).toUpperCase()}
                          </AvatarFallback>
                        </Avatar>
                        <span>{task.owner.nickname}</span>
                      </div>
                      <span>
                        {/* task.endAt은 Issue['endAt'] (Date) 타입입니다. */}
                        {new Date(task.endAt).toLocaleDateString('ko-KR', {
                          month: 'short',
                          day: 'numeric',
                        })}
                      </span>
                    </div>

                    {/* Labels */}
                    {task.labels && task.labels.length > 0 && (
                      <div className="flex flex-wrap gap-1">
                        {task.labels.map(label => (
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
                        ))}
                      </div>
                    )}
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
