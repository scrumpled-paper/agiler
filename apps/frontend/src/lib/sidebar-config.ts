// lib/sidebar-config.ts

export interface SidebarItem {
  title: string
  url: string
}

export interface SidebarSection {
  title: string
  items: SidebarItem[]
}

// 프로젝트 리스트 (실제로는 API에서 가져와야 함)
export const getProjectList = (): SidebarItem[] => [
  { title: 'Project 1', url: '/projects/1' },
  { title: 'Project 2', url: '/projects/2' },
]

// Dashboard 사이드바 구성
export const getDashboardSidebar = (): SidebarSection[] => [
  {
    title: 'Project list',
    items: getProjectList(),
  },
  {
    title: 'Settings',
    items: [{ title: 'General Settings', url: '/dashboard/settings' }],
  },
]

// Project 사이드바 구성
export const getProjectSidebar = (projectId: string): SidebarSection[] => [
  {
    title: 'Project list',
    items: getProjectList(),
  },
  {
    title: 'Project Settings',
    items: [{ title: 'Settings', url: `/projects/${projectId}/settings` }],
  },
  {
    title: 'Daily Scrum',
    items: [{ title: 'Scrum List', url: `/projects/${projectId}/daily-scrum` }],
  },
]
