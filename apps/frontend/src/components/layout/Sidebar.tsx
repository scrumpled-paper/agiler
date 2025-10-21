// components/layout/app-sidebar.tsx
import * as React from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'

import { SidebarUserInfo } from '@/components/layout/SidbarUserInfo'
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarRail,
} from '@/components/ui/sidebar'
import {
  getDashboardSidebar,
  getProjectSidebar,
  type SidebarSection,
} from '@/lib/sidebar-config'

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
  const location = useLocation()
  const params = useParams<{ projectId: string }>()

  // 현재 경로에 따라 사이드바 구성 결정
  const navSections = React.useMemo((): SidebarSection[] => {
    // /projects/:projectId 경로인지 확인
    if (location.pathname.startsWith('/projects/') && params.projectId) {
      return getProjectSidebar(params.projectId)
    }

    // /dashboard 경로인지 확인
    if (location.pathname.startsWith('/dashboard')) {
      return getDashboardSidebar()
    }

    // 기본값 (홈페이지 등)
    return getDashboardSidebar()
  }, [location.pathname, params.projectId])

  return (
    <Sidebar {...props}>
      <SidebarHeader>
        <SidebarUserInfo userName="user1" userInfo={['v1.0.0', 'v2.0.0']} />
      </SidebarHeader>
      <SidebarContent>
        {navSections.map(section => (
          <SidebarGroup key={section.title}>
            <SidebarGroupLabel>{section.title}</SidebarGroupLabel>
            <SidebarGroupContent>
              <SidebarMenu>
                {section.items.map(item => (
                  <SidebarMenuItem key={item.url}>
                    <SidebarMenuButton
                      asChild
                      isActive={location.pathname === item.url}
                    >
                      <Link to={item.url}>{item.title}</Link>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                ))}
              </SidebarMenu>
            </SidebarGroupContent>
          </SidebarGroup>
        ))}
      </SidebarContent>
      <SidebarRail />
    </Sidebar>
  )
}
