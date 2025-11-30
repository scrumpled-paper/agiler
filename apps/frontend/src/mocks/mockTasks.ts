import type { Issue, IssueColumn, UserInfo, Label, ProjectInfo } from '@/types'

// Mock Users
// UserInfo 타입은 이미 정의된 mockUsers 배열과 호환됩니다.
export const mockUsers: UserInfo[] = [
  { nickname: 'Alice Johnson', imageUrl: 'https://placehold.co/6x6' },
  { nickname: 'Bob Smith', imageUrl: 'https://placehold.co/6x6' },
  { nickname: 'Charlie Day', imageUrl: 'https://placehold.co/6x6' },
  { nickname: 'Dana Scully', imageUrl: 'https://placehold.co/6x6' },
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
  { name: 'Frontend', description: 'Client-side UI tasks', color: '#10B981' }, // green
  {
    name: 'Backend',
    description: 'Server-side logic and API tasks',
    color: '#F59E0B',
  }, // yellow/amber
  {
    name: 'Database',
    description: 'Schema and persistence tasks',
    color: '#3B82F6',
  }, // blue
  { name: 'Urgent', description: 'High priority tasks', color: '#EF4444' }, // red
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
  // Planned (col-2-planned)
  {
    id: 'task-1',
    name: 'Implement User Authentication',
    startAt: new Date('2025-11-01'),
    endAt: new Date('2025-11-15'),
    column: 'col-2-planned',
    owner: mockUsers[0], // Alice
    // 💡 optional properties added
    subscribers: [mockUsers[1], mockUsers[2]], // Bob, Charlie
    labels: [mockLabels[1], mockLabels[3]], // Backend, Urgent
  },
  {
    id: 'task-2',
    name: 'Design Landing Page Layout',
    startAt: new Date('2025-11-05'),
    endAt: new Date('2025-11-20'),
    column: 'col-2-planned',
    owner: mockUsers[1], // Bob
    labels: [mockLabels[0]], // Frontend
  },
  {
    id: 'task-3',
    name: 'Set up Database Schema',
    startAt: new Date('2025-11-08'),
    endAt: new Date('2025-11-18'),
    column: 'col-2-planned',
    owner: mockUsers[0], // Alice
    labels: [mockLabels[2]], // Database
  },

  // In Progress (col-3-progress)
  {
    id: 'task-4',
    name: 'Develop Kanban Card Component',
    startAt: new Date('2025-10-25'),
    endAt: new Date('2025-11-10'),
    column: 'col-3-progress',
    owner: mockUsers[2], // Charlie
    subscribers: [mockUsers[0], mockUsers[1], mockUsers[3]], // Alice, Bob, Dana (3명)
    labels: [mockLabels[0], mockLabels[3]], // Frontend, Urgent
  },
  {
    id: 'task-5',
    name: 'Write API Documentation',
    startAt: new Date('2025-11-03'),
    endAt: new Date('2025-11-12'),
    column: 'col-3-progress',
    owner: mockUsers[3], // Dana
    subscribers: [mockUsers[0], mockUsers[1]], // Alice, Bob
    labels: [mockLabels[1]], // Backend
  },

  // Done (col-4-done)
  {
    id: 'task-6',
    name: 'Project Initialization Complete',
    startAt: new Date('2025-10-15'),
    endAt: new Date('2025-10-20'),
    column: 'col-4-done',
    owner: mockUsers[1], // Bob
    labels: [mockLabels[0]], // Frontend
  },
  {
    id: 'task-7',
    name: 'Configure CI/CD Pipeline',
    startAt: new Date('2025-10-28'),
    endAt: new Date('2025-11-04'),
    column: 'col-4-done',
    owner: mockUsers[2], // Charlie
    subscribers: [mockUsers[3]], // Dana
    labels: [mockLabels[1]], // Backend
  },
  {
    id: 'task-8',
    name: 'First Deployment to Staging',
    startAt: new Date('2025-11-01'),
    endAt: new Date('2025-11-05'),
    column: 'col-4-done',
    owner: mockUsers[3], // Dana
    subscribers: [mockUsers[0], mockUsers[1], mockUsers[2], mockUsers[3]], // 4명 이상 (to test +N display)
    labels: [mockLabels[1], mockLabels[3]], // Backend, Urgent
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
