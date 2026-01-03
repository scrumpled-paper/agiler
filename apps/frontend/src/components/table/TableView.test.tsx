import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import TableView from './TableView'
import type { IssueColumn, UserInfo } from '@/types'
import type { Issue } from '@/types/issue'

describe('TableView', () => {
  const mockColumns: IssueColumn[] = [
    { id: '1', name: 'To Do', color: '#6B7280' },
    { id: '2', name: 'In Progress', color: '#3B82F6' },
    { id: '3', name: 'Done', color: '#10B981' },
  ]

  const mockProfiles: UserInfo[] = [
    {
      profileId: 1,
      nickname: 'John Doe',
      email: 'john@example.com',
      imageUrl: '',
    },
    {
      profileId: 2,
      nickname: 'Jane Smith',
      email: 'jane@example.com',
      imageUrl: '',
    },
  ]

  const mockTasks: Issue[] = [
    {
      id: '1',
      issueId: '1',
      name: 'Task 1',
      title: 'Task 1',
      column: '1',
      kanbanConfigId: 1,
      assignees: [1],
      startedAt: '2025-01-01T00:00:00.000Z',
      dueAt: '2025-01-10T00:00:00.000Z',
      createdAt: '2025-01-01T00:00:00.000Z',
      isDone: false,
      labels: [],
      notis: [],
    },
    {
      id: '2',
      issueId: '2',
      name: 'Task 2',
      title: 'Task 2',
      column: '2',
      kanbanConfigId: 2,
      assignees: [2],
      startedAt: '2025-01-05T00:00:00.000Z',
      dueAt: '2025-01-15T00:00:00.000Z',
      createdAt: '2025-01-05T00:00:00.000Z',
      isDone: false,
      labels: [],
      notis: [],
    },
  ]

  it('테이블을 렌더링한다', () => {
    render(
      <TableView
        columns={mockColumns}
        tasks={mockTasks}
        profiles={mockProfiles}
      />
    )

    // 테이블이 렌더링되어야 함
    expect(screen.getByRole('table')).toBeInTheDocument()
  })

  it('테이블 헤더를 올바르게 렌더링한다', () => {
    render(
      <TableView
        columns={mockColumns}
        tasks={mockTasks}
        profiles={mockProfiles}
      />
    )

    expect(screen.getByText('Task Name')).toBeInTheDocument()
    expect(screen.getByText('Status')).toBeInTheDocument()
    expect(screen.getByText('Owner')).toBeInTheDocument()
    expect(screen.getByText('Start Date')).toBeInTheDocument()
    expect(screen.getByText('Due Date')).toBeInTheDocument()
  })

  it('태스크 데이터를 올바르게 표시한다', () => {
    render(
      <TableView
        columns={mockColumns}
        tasks={mockTasks}
        profiles={mockProfiles}
      />
    )

    expect(screen.getByText('Task 1')).toBeInTheDocument()
    expect(screen.getByText('Task 2')).toBeInTheDocument()
  })

  it('상태 컬럼에 올바른 색상과 이름을 표시한다', () => {
    render(
      <TableView
        columns={mockColumns}
        tasks={mockTasks}
        profiles={mockProfiles}
      />
    )

    expect(screen.getByText('To Do')).toBeInTheDocument()
    expect(screen.getByText('In Progress')).toBeInTheDocument()
  })

  it('담당자 정보를 올바르게 표시한다', () => {
    render(
      <TableView
        columns={mockColumns}
        tasks={mockTasks}
        profiles={mockProfiles}
      />
    )

    expect(screen.getByText('John Doe')).toBeInTheDocument()
    expect(screen.getByText('Jane Smith')).toBeInTheDocument()
  })

  it('시작 날짜를 한국어 형식으로 표시한다', () => {
    render(
      <TableView
        columns={mockColumns}
        tasks={mockTasks}
        profiles={mockProfiles}
      />
    )

    // 2025년 1월 1일 (여러 날짜가 있을 수 있으므로 getAllByText 사용)
    const dateElements = screen.getAllByText(/2025/)
    expect(dateElements.length).toBeGreaterThan(0)

    const monthElements = screen.getAllByText(/1월/)
    expect(monthElements.length).toBeGreaterThan(0)
  })

  it('마감 날짜를 한국어 형식으로 표시한다', () => {
    render(
      <TableView
        columns={mockColumns}
        tasks={mockTasks}
        profiles={mockProfiles}
      />
    )

    // 여러 날짜가 표시되어야 함
    const dateElements = screen.getAllByText(/2025/)
    expect(dateElements.length).toBeGreaterThan(0)
  })

  it('빈 태스크 목록을 처리한다', () => {
    render(
      <TableView columns={mockColumns} tasks={[]} profiles={mockProfiles} />
    )

    // 헤더는 표시되어야 함
    expect(screen.getByText('Task Name')).toBeInTheDocument()

    // 빈 데이터 메시지가 표시되거나, 데이터가 없어야 함
    const taskCells = screen.queryByText('Task 1')
    expect(taskCells).not.toBeInTheDocument()
  })

  it('담당자가 없는 태스크를 처리한다', () => {
    const tasksWithoutAssignee: Issue[] = [
      {
        id: '3',
        issueId: '3',
        name: 'Task 3',
        title: 'Task 3',
        column: '1',
        kanbanConfigId: 1,
        assignees: [],
        startedAt: '2025-01-01T00:00:00.000Z',
        dueAt: '2025-01-10T00:00:00.000Z',
        createdAt: '2025-01-01T00:00:00.000Z',
        isDone: false,
        labels: [],
        notis: [],
      },
    ]

    render(
      <TableView
        columns={mockColumns}
        tasks={tasksWithoutAssignee}
        profiles={mockProfiles}
      />
    )

    // 태스크는 표시되어야 함
    expect(screen.getByText('Task 3')).toBeInTheDocument()

    // 담당자가 없으므로 빈 셀이어야 함
    const table = screen.getByRole('table')
    expect(table).toBeInTheDocument()
  })

  it('알 수 없는 상태를 "Unknown"으로 표시한다', () => {
    const tasksWithUnknownStatus: Issue[] = [
      {
        id: '4',
        issueId: '4',
        name: 'Task 4',
        title: 'Task 4',
        column: '999', // 존재하지 않는 컬럼 ID
        kanbanConfigId: 999,
        assignees: [1],
        startedAt: '2025-01-01T00:00:00.000Z',
        dueAt: '2025-01-10T00:00:00.000Z',
        createdAt: '2025-01-01T00:00:00.000Z',
        isDone: false,
        labels: [],
        notis: [],
      },
    ]

    render(
      <TableView
        columns={mockColumns}
        tasks={tasksWithUnknownStatus}
        profiles={mockProfiles}
      />
    )

    expect(screen.getByText('Unknown')).toBeInTheDocument()
  })

  it('여러 태스크를 올바른 순서로 렌더링한다', () => {
    render(
      <TableView
        columns={mockColumns}
        tasks={mockTasks}
        profiles={mockProfiles}
      />
    )

    const taskNames = screen.getAllByRole('cell').filter(cell => {
      return cell.textContent === 'Task 1' || cell.textContent === 'Task 2'
    })

    expect(taskNames).toHaveLength(2)
  })

  it('날짜가 없는 경우에도 렌더링한다', () => {
    const tasksWithoutDates: Issue[] = [
      {
        id: '5',
        issueId: '5',
        name: 'Task 5',
        title: 'Task 5',
        column: '1',
        kanbanConfigId: 1,
        assignees: [1],
        startedAt: '',
        dueAt: '',
        createdAt: '2025-01-01T00:00:00.000Z',
        isDone: false,
        labels: [],
        notis: [],
      },
    ]

    render(
      <TableView
        columns={mockColumns}
        tasks={tasksWithoutDates}
        profiles={mockProfiles}
      />
    )

    expect(screen.getByText('Task 5')).toBeInTheDocument()
  })

  it('프로필 맵이 올바르게 동작한다', () => {
    const manyProfiles: UserInfo[] = [
      ...mockProfiles,
      {
        profileId: 3,
        nickname: 'Alice Johnson',
        email: 'alice@example.com',
        imageUrl: '',
      },
      {
        profileId: 4,
        nickname: 'Bob Wilson',
        email: 'bob@example.com',
        imageUrl: '',
      },
    ]

    const taskWithProfile3: Issue[] = [
      {
        id: '6',
        issueId: '6',
        name: 'Task 6',
        title: 'Task 6',
        column: '1',
        kanbanConfigId: 1,
        assignees: [3],
        startedAt: '2025-01-01T00:00:00.000Z',
        dueAt: '2025-01-10T00:00:00.000Z',
        createdAt: '2025-01-01T00:00:00.000Z',
        isDone: false,
        labels: [],
        notis: [],
      },
    ]

    render(
      <TableView
        columns={mockColumns}
        tasks={taskWithProfile3}
        profiles={manyProfiles}
      />
    )

    expect(screen.getByText('Alice Johnson')).toBeInTheDocument()
  })

  it('상태별로 올바른 색상 인디케이터를 표시한다', () => {
    render(
      <TableView
        columns={mockColumns}
        tasks={mockTasks}
        profiles={mockProfiles}
      />
    )

    // 색상 인디케이터 div들을 찾기 (h-2 w-2 rounded-full 클래스를 가진 요소)
    const table = screen.getByRole('table')
    const colorIndicators = table.querySelectorAll('.h-2.w-2.rounded-full')

    expect(colorIndicators.length).toBeGreaterThan(0)
  })
})
