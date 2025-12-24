/* cSpell:disable */
import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import KanbanView from './KanbanView'
import type { IssueColumn, UserInfo } from '@/types'
import type { Issue } from '@/types/issue'
import type { Label } from '@/types/label'

// Mock IssueModal 컴포넌트
vi.mock('@/components/IssueModal', () => ({
  IssueModal: () => null,
}))

// Mock 데이터
const mockColumns: IssueColumn[] = [
  { id: '1', name: 'To Do' },
  { id: '2', name: 'In Progress' },
  { id: '3', name: 'Done' },
]

const mockProfiles: UserInfo[] = [
  {
    profileId: 1,
    nickname: 'Alice',
    email: 'alice@example.com',
    imageUrl: 'https://placehold.co/100x100',
    role: 'OWNER',
  },
  {
    profileId: 2,
    nickname: 'Bob',
    email: 'bob@example.com',
    imageUrl: 'https://placehold.co/100x100',
    role: 'MEMBER',
  },
]

const mockLabels: Label[] = [
  {
    labelId: 1,
    name: 'bug',
    description: 'Bug label',
    color: '#FF4040',
  },
  {
    labelId: 2,
    name: 'feature',
    description: 'Feature label',
    color: '#4040FF',
  },
]

const mockTasks: Issue[] = [
  {
    id: '1',
    issueId: '1',
    name: 'Fix login bug',
    title: 'Fix login bug',
    column: '1',
    kanbanConfigId: 1,
    dueAt: '2025-12-31T00:00:00Z',
    startedAt: '2025-12-01T00:00:00Z',
    createdAt: '2025-12-01T00:00:00Z',
    assignees: [1],
    labels: [1],
    notis: [1, 2],
    isDone: false,
  },
  {
    id: '2',
    issueId: '2',
    name: 'Implement dark mode',
    title: 'Implement dark mode',
    column: '2',
    kanbanConfigId: 2,
    dueAt: '2025-12-25T00:00:00Z',
    startedAt: '2025-12-10T00:00:00Z',
    createdAt: '2025-12-10T00:00:00Z',
    assignees: [2],
    labels: [2],
    notis: [2],
    isDone: false,
  },
  {
    id: '3',
    issueId: '3',
    name: 'Update documentation',
    title: 'Update documentation',
    column: '3',
    kanbanConfigId: 3,
    dueAt: '2025-12-20T00:00:00Z',
    startedAt: '2025-12-05T00:00:00Z',
    createdAt: '2025-12-05T00:00:00Z',
    assignees: [1, 2],
    labels: [1, 2],
    notis: [],
    isDone: true,
  },
]

describe('KanbanView - 통합 테스트', () => {
  describe('기본 렌더링', () => {
    it('컬럼 헤더를 렌더링한다', () => {
      render(
        <KanbanView
          columns={mockColumns}
          tasks={mockTasks}
          labels={mockLabels}
          profiles={mockProfiles}
        />
      )

      expect(screen.getByText('To Do')).toBeInTheDocument()
      expect(screen.getByText('In Progress')).toBeInTheDocument()
      expect(screen.getByText('Done')).toBeInTheDocument()
    })

    it('필터 바가 렌더링된다', () => {
      render(
        <KanbanView
          columns={mockColumns}
          tasks={mockTasks}
          labels={mockLabels}
          profiles={mockProfiles}
        />
      )

      // 검색 입력 필드가 있는지 확인
      expect(screen.getByPlaceholderText(/이슈 검색/i)).toBeInTheDocument()
    })

    it('정렬 옵션이 표시된다', () => {
      render(
        <KanbanView
          columns={mockColumns}
          tasks={mockTasks}
          labels={mockLabels}
          profiles={mockProfiles}
        />
      )

      // 정렬 드롭다운 버튼이 있는지 확인
      expect(screen.getByText(/마감일 빠른순/i)).toBeInTheDocument()
    })

    it('필터 버튼들이 렌더링된다', () => {
      render(
        <KanbanView
          columns={mockColumns}
          tasks={mockTasks}
          labels={mockLabels}
          profiles={mockProfiles}
        />
      )

      // 담당자, 라벨, 구독자 필터 버튼이 있는지 확인
      expect(screen.getByText('담당자')).toBeInTheDocument()
      expect(screen.getByText('라벨')).toBeInTheDocument()
      expect(screen.getByText('구독자')).toBeInTheDocument()
    })
  })

  describe('읽기 전용 모드', () => {
    it('isReadOnly가 true일 때 경고 메시지를 표시한다', () => {
      render(
        <KanbanView
          columns={mockColumns}
          tasks={mockTasks}
          labels={mockLabels}
          profiles={mockProfiles}
          isReadOnly={true}
        />
      )

      expect(
        screen.getByText('과거 날짜의 칸반은 수정할 수 없습니다.')
      ).toBeInTheDocument()
    })

    it('isReadOnly가 false일 때 경고 메시지를 표시하지 않는다', () => {
      render(
        <KanbanView
          columns={mockColumns}
          tasks={mockTasks}
          labels={mockLabels}
          profiles={mockProfiles}
          isReadOnly={false}
        />
      )

      expect(
        screen.queryByText('과거 날짜의 칸반은 수정할 수 없습니다.')
      ).not.toBeInTheDocument()
    })
  })

  describe('빈 상태 처리', () => {
    it('태스크가 없을 때 빈 칸반 보드를 표시한다', () => {
      render(
        <KanbanView
          columns={mockColumns}
          tasks={[]}
          labels={mockLabels}
          profiles={mockProfiles}
        />
      )

      // 컬럼 헤더는 표시되어야 함
      expect(screen.getByText('To Do')).toBeInTheDocument()
      expect(screen.getByText('In Progress')).toBeInTheDocument()
      expect(screen.getByText('Done')).toBeInTheDocument()
    })

    it('라벨이 없을 때도 정상 렌더링된다', () => {
      const { container } = render(
        <KanbanView
          columns={mockColumns}
          tasks={mockTasks}
          labels={[]}
          profiles={mockProfiles}
        />
      )

      // 컴포넌트가 렌더링되었는지 확인
      expect(
        container.querySelector('.flex.flex-col.gap-4')
      ).toBeInTheDocument()
    })

    it('프로필이 없을 때도 정상 렌더링된다', () => {
      const { container } = render(
        <KanbanView
          columns={mockColumns}
          tasks={mockTasks}
          labels={mockLabels}
          profiles={[]}
        />
      )

      // 컴포넌트가 렌더링되었는지 확인
      expect(
        container.querySelector('.flex.flex-col.gap-4')
      ).toBeInTheDocument()
    })

    it('컬럼이 없을 때도 에러 없이 렌더링된다', () => {
      const { container } = render(
        <KanbanView
          columns={[]}
          tasks={mockTasks}
          labels={mockLabels}
          profiles={mockProfiles}
        />
      )

      // 필터 바는 렌더링되어야 함
      expect(screen.getByPlaceholderText(/이슈 검색/i)).toBeInTheDocument()
      expect(
        container.querySelector('.flex.flex-col.gap-4')
      ).toBeInTheDocument()
    })
  })

  describe('Props 전달', () => {
    it('columns prop을 올바르게 받는다', () => {
      render(
        <KanbanView
          columns={mockColumns}
          tasks={mockTasks}
          labels={mockLabels}
          profiles={mockProfiles}
        />
      )

      // 각 컬럼이 렌더링되었는지 확인
      mockColumns.forEach(column => {
        expect(screen.getByText(column.name)).toBeInTheDocument()
      })
    })

    it('tasks prop을 올바르게 받는다', () => {
      const { container } = render(
        <KanbanView
          columns={mockColumns}
          tasks={mockTasks}
          labels={mockLabels}
          profiles={mockProfiles}
        />
      )

      // KanbanProvider가 렌더링되었는지 확인 (tasks를 사용하는 컴포넌트)
      expect(
        container.querySelector('.flex.flex-col.gap-4')
      ).toBeInTheDocument()
    })

    it('labels와 profiles prop을 올바르게 받는다', () => {
      render(
        <KanbanView
          columns={mockColumns}
          tasks={mockTasks}
          labels={mockLabels}
          profiles={mockProfiles}
        />
      )

      // 필터 버튼들이 렌더링되었는지 확인 (labels와 profiles를 사용)
      expect(screen.getByText('담당자')).toBeInTheDocument()
      expect(screen.getByText('라벨')).toBeInTheDocument()
    })
  })
})
