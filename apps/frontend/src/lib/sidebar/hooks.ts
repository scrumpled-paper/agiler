import { useQuery } from '@tanstack/react-query'
import { useLocation, useParams } from 'react-router-dom'
import type { SidebarContext, SidebarData } from './types'
import { getSidebarContext } from './config'
import { projectService } from '@/api/services/projectService'
import type { ProjectInfo } from '@/types'
import { userService } from '@/api/services/userService'

/**
 * 현재 경로를 기반으로 사이드바 컨텍스트를 반환하는 훅
 */
export const useSidebarContext = (): SidebarContext => {
  const location = useLocation()
  return getSidebarContext(location.pathname)
}

/**
 * 프로젝트 리스트를 페칭하는 훅
 */
export const useProjectList = () => {
  return useQuery<ProjectInfo[]>({
    queryKey: ['projects', 'sidebar'],
    queryFn: async () => {
      // 사이드바용: 페이지네이션 없이 모든 프로젝트 조회
      const response = await projectService.getProjectSidebar({
        page: 0,
        size: 100,
      })
      return response.contents
    },
  })
}

/**
 * 프로젝트 멤버 리스트를 페칭하는 훅
 */
export const useProjectMembers = (projectUrl?: string, enabled = true) => {
  return useQuery({
    queryKey: ['projectMembers', projectUrl],
    queryFn: () => {
      if (!projectUrl) throw new Error('projectUrl is required')

      console.log(
        '[useProjectMembers] Fetching members for projectUrl:',
        projectUrl
      )
      return projectService.getProjectMember({
        projectUrl,
        size: 10,
        page: 0,
      })
    },
    enabled: enabled && !!projectUrl,
  })
}

/**
 * 사이드바에 필요한 모든 데이터를 페칭하는 메인 훅
 */
export const useSidebarData = (
  context: SidebarContext,
  projectUrl?: string
): SidebarData => {
  // 프로젝트 리스트는 모든 컨텍스트에서 필요
  const { data: projectsData } = useProjectList()

  // 멤버는 project 컨텍스트에서만 필요
  const { data: membersData } = useProjectMembers(
    projectUrl,
    context === 'project'
  )

  // UserInfo는 context에 따라 다른 API 호출
  // dashboard: userService.getUserInfo()
  // project: projectService.getUserInfo(projectUrl)
  const { data: dashboardUserInfo } = useQuery({
    queryKey: ['userInfo', 'dashboard'],
    queryFn: async () => {
      console.log('[useSidebarData] Fetching dashboard user info')
      const response = await userService.getUserInfo()
      return response
    },
    enabled: context === 'dashboard',
    staleTime: 5 * 60 * 1000,
  })

  const { data: projectUserInfo } = useQuery({
    queryKey: ['userInfo', 'project', projectUrl],
    queryFn: async () => {
      if (!projectUrl) throw new Error('projectUrl is required')
      console.log(
        '[useSidebarData] Fetching project user info for:',
        projectUrl
      )
      const response = await projectService.getUserInfo(projectUrl)
      return response
    },
    enabled: context === 'project' && !!projectUrl,
    staleTime: 5 * 60 * 1000,
  })

  return {
    projects: projectsData,
    members: membersData?.contents,
    userInfo: context === 'dashboard' ? dashboardUserInfo : projectUserInfo,
  }
}

/**
 * 현재 라우터 파라미터를 반환하는 유틸리티 훅
 */
export const useSidebarParams = () => {
  const params = useParams<{ projectUrl: string }>()
  return params
}
