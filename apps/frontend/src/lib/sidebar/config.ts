import { User } from 'lucide-react'
import type { SidebarConfig, SidebarContext } from './types'

export const sidebarConfigs: Record<SidebarContext, SidebarConfig> = {
  dashboard: {
    header: { type: 'user-info' },
    sections: [
      {
        type: 'list',
        title: 'Project List',
        // icon: User,
        dataKey: 'projects',
        hasShowMore: false, // [ ] UI 개선 후 로직 변경
        showMoreRoute: '/dashboard/projects',
      },
      {
        type: 'navigation',
        displayTitle: false,
        title: 'navigation list',
        items: [
          {
            label: 'Settings',
            // icon: Settings,
            route: '/dashboard/settings',
          },
          // { label: 'Help', route: '/help' },
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
        type: 'action',
        title: 'Notification',
        action: {
          label: 'Notification',
          onClick: 'notification',
        },
      },
      // [ ] UI 개선을 위해 남겨둠
      // {
      //   type: 'list',
      //   title: 'Project List',
      //   dataKey: 'projects',
      //   hasShowMore: true,
      //   showMoreRoute: '/dashboard/projects',
      // },
      {
        type: 'navigation',
        title: 'navigation list',
        displayTitle: false,
        items: [
          {
            label: 'Home',
            route: ':projectUrl',
          },
          {
            label: 'Daily Scrums',
            route: ':projectUrl/dailyscrums',
          },
          {
            label: 'Retrospectives',
            route: ':projectUrl/retrospectives',
          },
          {
            label: 'Meetings',
            route: ':projectUrl/meetings',
          },
          {
            label: 'Settings',
            // icon: Settings,
            route: ':projectUrl/settings',
          },
        ],
      },
      // 프로젝트 참가 링크 ui 개선될 수 있어서 남겨두었습니다.
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
        icon: User,
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
    // header: { type: 'user-info' },
    sections: [
      {
        type: 'navigation',
        title: 'Project Settings',
        displayTitle: true,
        // icon: User,
        items: [
          { label: 'User Profile', route: ':projectUrl/settings/users' },
          {
            label: 'Project Management',
            route: ':projectUrl/settings/project-management',
          },
          {
            label: 'Label Setting',
            route: ':projectUrl/settings/project-label',
          },
          {
            label: 'Template Setting',
            route: ':projectUrl/settings/project-template',
          },
          {
            label: 'Notifications Setting',
            route: ':projectUrl/settings/notifications',
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
