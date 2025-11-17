// lib/sidebar/config.ts

import type { SidebarConfig, SidebarContext } from './types'

export const sidebarConfigs: Record<SidebarContext, SidebarConfig> = {
  dashboard: {
    header: { type: 'user-info' },
    sections: [
      {
        type: 'list',
        title: 'Project List',
        icon: '😃',
        dataKey: 'projects',
        hasShowMore: true,
        showMoreRoute: '/dashboard/projects',
      },
      {
        type: 'navigation',
        displayTitle: false,
        title: 'navigation list',
        items: [
          { label: 'Settings', route: '/dashboard/settings' },
          { label: 'Help', route: '/help' },
        ],
      },
      {
        type: 'action',
        title: 'Logout',
        action: {
          label: 'Logout',
          onClick: 'logoutLink',
        },
      },
    ],
  },

  project: {
    header: { type: 'user-info' },
    sections: [
      {
        type: 'list',
        title: 'Project List',
        icon: '📂',
        dataKey: 'projects',
        hasShowMore: true,
        showMoreRoute: '/dashboard/projects',
      },
      {
        type: 'navigation',
        title: 'navigation list',
        displayTitle: false,
        items: [
          { label: 'Settings', icon: '🔧', route: ':projectUrl/settings' },
          {
            label: 'Daily Scrum',
            icon: '📆',
            route: ':projectUrl/daily-scrum',
          },
          { label: 'Retrospect', icon: '📒', route: ':projectUrl/retrospect' },
          { label: 'Meeting', icon: '📝', route: ':projectUrl/meeting' },
        ],
      },
      // {
      //   type: 'action',
      //   title: 'Members',
      //   action: {
      //     label: '프로젝트 참가 링크 생성',
      //     onClick: 'generateLink',
      //   },
      // },
      {
        type: 'display',
        title: 'Members',
        icon: '👥',
        dataKey: 'members',
        hasShowMore: true,
        showMoreRoute: ':projectUrl/settings/members',
      },
      {
        type: 'action',
        title: 'Logout',
        action: {
          label: 'Logout',
          onClick: 'logoutLink',
        },
      },
    ],
  },

  'project-settings': {
    header: { type: 'user-info' },
    sections: [
      {
        type: 'navigation',
        title: 'Project Settings',
        displayTitle: true,
        icon: '👤',
        items: [
          { label: 'User Profile', route: ':projectUrl/settings/profile' },
          {
            label: 'Project Management',
            route: ':projectUrl/settings/project',
          },
          {
            label: 'Database Management',
            route: ':projectUrl/settings/database',
          },
          { label: 'API Settings', route: ':projectUrl/settings/api' },
          {
            label: 'Additional Settings',
            route: ':projectUrl/settings/additional',
          },
        ],
      },
    ],
  },
}

export const getSidebarContext = (pathname: string): SidebarContext => {
  if (pathname.includes('/settings')) return 'project-settings'
  if (pathname.startsWith('/projects/')) return 'project'
  return 'dashboard'
}
