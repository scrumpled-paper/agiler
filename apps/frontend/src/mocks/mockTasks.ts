import type { IssueColumn, UserInfo, ProjectInfo } from '@/types'
import type { Issue } from '@/types/issue'
import type { Label } from '@/types/label'
import type { PagedResponse } from '@/types/list'

// Mock Users
// UserInfo 타입은 이미 정의된 mockUsers 배열과 호환됩니다.
export const mockUsers: UserInfo[] = [
  {
    profileId: 1,
    nickname: 'Alice Johnson',
    imageUrl: 'https://placehold.co/6x6',
  },
  { profileId: 2, nickname: 'Bob Smith', imageUrl: 'https://placehold.co/6x6' },
  {
    profileId: 3,
    nickname: 'Charlie Day',
    imageUrl: 'https://placehold.co/6x6',
  },
  {
    profileId: 4,
    nickname: 'Dana Scully',
    imageUrl: 'https://placehold.co/6x6',
  },
]

export const MOCK_MEMBER_PROFILES = [
  {
    profileId: 101,
    nickname: 'Alice_Owner',
    email: 'alice@projectagile.com',
    imageUrl: 'https://placehold.co/100x100/007bff/ffffff?text=A',
    role: 'OWNER',
    description: '프로젝트의 오너이자 주요 기획 담당자입니다.',
  },
  {
    profileId: 102,
    nickname: 'Bob_Manager',
    email: 'bob@projectagile.com',
    imageUrl: 'https://placehold.co/100x100/28a745/ffffff?text=B',
    role: 'MANAGER',
    description: '스프린트 관리 및 팀 리소스를 조율하는 매니저입니다.',
  },
  {
    profileId: 103,
    nickname: 'Charlie_Member',
    email: 'charlie@projectagile.com',
    imageUrl: 'https://placehold.co/100x100/ffc107/343a40?text=C',
    role: 'MEMBER',
    description: '프론트엔드 개발 및 이슈 해결을 담당하는 팀원입니다.',
  },
]

// Mock Labels (새로 추가)
export const mockLabels: Label[] = [
  {
    labelId: 1,
    name: 'Frontend',
    description: 'Client-side UI tasks',
    color: '#10B981',
  },
  {
    labelId: 2,
    name: 'Backend',
    description: 'Server-side logic and API tasks',
    color: '#F59E0B',
  },
  {
    labelId: 3,
    name: 'Database',
    description: 'Schema and persistence tasks',
    color: '#3B82F6',
  },
  {
    labelId: 4,
    name: 'Urgent',
    description: 'High priority tasks',
    color: '#EF4444',
  },
]

// Issue Columns (Kanban 컬럼)
// taskColumns -> issueColumns로 이름 변경 (타입 IssueColumn과의 일관성을 위해)
export const issueColumns: IssueColumn[] = [
  { id: 'col-1-backlog', name: 'Back Log', color: '#6B7280' },
  { id: 'col-2-planned', name: 'Planned', color: '#6B7280' },
  { id: 'col-3-progress', name: 'In Progress', color: '#F59E0B' },
  { id: 'col-4-done', name: 'Done', color: '#10B981' },
]

// Mock Issues
export const mockIssues: Issue[] = [
  // Planned (kanbanConfigId: 2)
  {
    // 호환용 필드
    id: '1',
    name: 'Implement User Authentication',
    column: '2',
    // 신규 API 명세 필드
    issueId: '1',
    title: 'Implement User Authentication',
    kanbanConfigId: 2,
    assignees: [1],
    notis: [2, 3],
    labels: [2, 4],
    startedAt: '2025-11-01T00:00:00Z',
    dueAt: '2025-11-15T00:00:00Z',
    createdAt: '2025-10-20T00:00:00Z',
    isDone: false,
  },
  {
    id: '2',
    name: 'Design Landing Page Layout',
    column: '2',
    issueId: '2',
    title: 'Design Landing Page Layout',
    kanbanConfigId: 2,
    assignees: [2],
    notis: [],
    labels: [1],
    startedAt: '2025-11-05T00:00:00Z',
    dueAt: '2025-11-20T00:00:00Z',
    createdAt: '2025-10-21T00:00:00Z',
    isDone: false,
  },
  {
    id: '3',
    name: 'Set up Database Schema',
    column: '2',
    issueId: '3',
    title: 'Set up Database Schema',
    kanbanConfigId: 2,
    assignees: [1],
    notis: [],
    labels: [3],
    startedAt: '2025-11-08T00:00:00Z',
    dueAt: '2025-11-18T00:00:00Z',
    createdAt: '2025-10-22T00:00:00Z',
    isDone: false,
  },

  // In Progress (kanbanConfigId: 3)
  {
    id: '4',
    name: 'Develop Kanban Card Component',
    column: '3',
    issueId: '4',
    title: 'Develop Kanban Card Component',
    kanbanConfigId: 3,
    assignees: [3],
    notis: [1, 2, 4],
    labels: [1, 4],
    startedAt: '2025-10-25T00:00:00Z',
    dueAt: '2025-11-10T00:00:00Z',
    createdAt: '2025-10-20T00:00:00Z',
    isDone: false,
  },
  {
    id: '5',
    name: 'Write API Documentation',
    column: '3',
    issueId: '5',
    title: 'Write API Documentation',
    kanbanConfigId: 3,
    assignees: [4],
    notis: [1, 2],
    labels: [2],
    startedAt: '2025-11-03T00:00:00Z',
    dueAt: '2025-11-12T00:00:00Z',
    createdAt: '2025-10-25T00:00:00Z',
    isDone: false,
  },

  // Done (kanbanConfigId: 4)
  {
    id: '6',
    name: 'Project Initialization Complete',
    column: '4',
    issueId: '6',
    title: 'Project Initialization Complete',
    kanbanConfigId: 4,
    assignees: [2],
    notis: [],
    labels: [1],
    startedAt: '2025-10-15T00:00:00Z',
    dueAt: '2025-10-20T00:00:00Z',
    createdAt: '2025-10-10T00:00:00Z',
    isDone: true,
  },
  {
    id: '7',
    name: 'Configure CI/CD Pipeline',
    column: '4',
    issueId: '7',
    title: 'Configure CI/CD Pipeline',
    kanbanConfigId: 4,
    assignees: [3],
    notis: [4],
    labels: [2],
    startedAt: '2025-10-28T00:00:00Z',
    dueAt: '2025-11-04T00:00:00Z',
    createdAt: '2025-10-20T00:00:00Z',
    isDone: true,
  },
  {
    id: '8',
    name: 'First Deployment to Staging',
    column: '4',
    issueId: '8',
    title: 'First Deployment to Staging',
    kanbanConfigId: 4,
    assignees: [4],
    notis: [1, 2, 3, 4],
    labels: [2, 4],
    startedAt: '2025-11-01T00:00:00Z',
    dueAt: '2025-11-05T00:00:00Z',
    createdAt: '2025-10-30T00:00:00Z',
    isDone: true,
  },
]
export const mockProjectList: ProjectInfo[] = [
  {
    title: 'Agile Project',
    url: 'agile-project',
    imageUrl: 'https://placehold.co/600x400',
    summary: 'An agile project management tool',
  },
  {
    title: 'Design System',
    url: 'design-system1',
    imageUrl: 'https://placehold.co/600x400',
    summary: 'Company-wide design system',
  },
  {
    title: 'Design System2',
    url: 'design-system2',
    imageUrl: 'https://placehold.co/600x400',
    summary: 'Company-wide design system2',
  },
  {
    title: 'Design System3',
    url: 'design-system3',
    imageUrl: 'https://placehold.co/600x400',
    summary: 'Company-wide design system3',
  },
]

// 예제 데이터
export const mockListData: PagedResponse<{
  id: number
  title: string
  createdAt: string
  participants: Array<{
    id: number
    nickname: string
    imageUrl: string
  }>
}> = {
  contents: [
    {
      id: 1,
      title: '스프린트 1 회고',
      createdAt: '2024-01-15T10:00:00',
      participants: [
        {
          id: 1,
          nickname: '김철수',
          imageUrl: 'https://github.com/shadcn.png',
        },
        {
          id: 2,
          nickname: '이영희',
          imageUrl: 'https://github.com/vercel.png',
        },
      ],
    },
    {
      id: 2,
      title: '데일리 스크럼 - 2024.01.14',
      createdAt: '2024-01-14T09:30:00',
      participants: [
        {
          id: 1,
          nickname: '김철수',
          imageUrl: 'https://github.com/shadcn.png',
        },
        {
          id: 2,
          nickname: '이영희',
          imageUrl: 'https://github.com/vercel.png',
        },
        {
          id: 3,
          nickname: '박민수',
          imageUrl: 'https://github.com/react.png',
        },
        {
          id: 4,
          nickname: '최지민',
          imageUrl: 'https://github.com/facebook.png',
        },
      ],
    },
    {
      id: 3,
      title: '데일리 스크럼 - 2024.01.14',
      createdAt: '2024-01-14T09:30:00',
      participants: [
        {
          id: 1,
          nickname: '김철수',
          imageUrl: 'https://github.com/shadcn.png',
        },
      ],
    },
    {
      id: 3,
      title: '데일리 스크럼 - 2024.01.14',
      createdAt: '2024-01-14T09:30:00',
      participants: [
        {
          id: 1,
          nickname: '김철수',
          imageUrl: 'https://github.com/shadcn.png',
        },
      ],
    },
    {
      id: 3,
      title: '데일리 스크럼 - 2024.01.14',
      createdAt: '2024-01-14T09:30:00',
      participants: [
        {
          id: 1,
          nickname: '김철수',
          imageUrl: 'https://github.com/shadcn.png',
        },
      ],
    },
    {
      id: 3,
      title: '데일리 스크럼 - 2024.01.14',
      createdAt: '2024-01-14T09:30:00',
      participants: [
        {
          id: 1,
          nickname: '김철수',
          imageUrl: 'https://github.com/shadcn.png',
        },
      ],
    },
    {
      id: 3,
      title: '데일리 스크럼 - 2024.01.14',
      createdAt: '2024-01-14T09:30:00',
      participants: [
        {
          id: 1,
          nickname: '김철수',
          imageUrl: 'https://github.com/shadcn.png',
        },
      ],
    },
    {
      id: 3,
      title: '데일리 스크럼 - 2024.01.14',
      createdAt: '2024-01-14T09:30:00',
      participants: [
        {
          id: 1,
          nickname: '김철수',
          imageUrl: 'https://github.com/shadcn.png',
        },
      ],
    },
    {
      id: 3,
      title: '데일리 스크럼 - 2024.01.14',
      createdAt: '2024-01-14T09:30:00',
      participants: [
        {
          id: 1,
          nickname: '김철수',
          imageUrl: 'https://github.com/shadcn.png',
        },
      ],
    },
    {
      id: 3,
      title: '데일리 스크럼 - 2024.01.14',
      createdAt: '2024-01-14T09:30:00',
      participants: [
        {
          id: 1,
          nickname: '김철수',
          imageUrl: 'https://github.com/shadcn.png',
        },
      ],
    },
  ],
  size: 10,
  number: 1,
  totalPages: 1,
}
