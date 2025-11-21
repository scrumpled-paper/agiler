import { useQuery } from '@tanstack/react-query'
import { useLocation, useParams } from 'react-router-dom'
import type { SidebarContext, SidebarData } from './types'
import { getSidebarContext } from './config'
import { projectService } from '@/api/services/projectService'
import type { ProjectInfo, UserInfo } from '@/types'
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

  return {
    projects: projectsData,
    members: membersData?.contents,
  }
}

// 유저 정보를 불러오는 훅
export const useUserInfo = () => {
  return useQuery<UserInfo>({
    // 이 키는 앱 전역에서 사용자 정보를 참조하고 업데이트할 수 있게 합니다.
    queryKey: ['userInfo'],
    queryFn: async () => {
      console.log('[useUserInfo] Fetching current user info')
      const response = await userService.getUserInfo()
      return response // API 응답 데이터 (User 객체)
    },
    // 앱이 처음 로드될 때 항상 유저 정보가 필요하므로 enabled는 별도 설정이 필요하지 않습니다.
    staleTime: 5 * 60 * 1000, // 예: 5분 동안은 데이터를 신선하게 유지
  })
}

/**
 * 현재 라우터 파라미터를 반환하는 유틸리티 훅
 */
export const useSidebarParams = () => {
  const params = useParams<{ projectUrl: string }>()
  return params
}
