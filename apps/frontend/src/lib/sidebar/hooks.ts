// lib/sidebar/hooks.ts

import { useQuery } from '@tanstack/react-query'
import { useLocation, useParams } from 'react-router-dom'
import type { SidebarContext, SidebarData } from './types'
import { getSidebarContext } from './config'
import { generateMockContents } from '@/utils/mockData'
import { fetchMockProjectMembers } from '@/utils/mockData'
import type { ProjectInfo } from '@/types'

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
    queryKey: ['projects'],
    queryFn: async () => {
      // TODO: 실제 API 호출로 대체
      // 현재는 mock 데이터 사용
      const mockContents = generateMockContents(10)
      return mockContents.map(item => ({
        title: item.title,
        url: item.url,
        imageUrl: item.imageUrl,
        summary: item.summary,
      }))
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

      return fetchMockProjectMembers({
        projectUrl: Number(projectUrl),
        size: 5,
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

/**
 * 현재 라우터 파라미터를 반환하는 유틸리티 훅
 */
export const useSidebarParams = () => {
  const params = useParams<{ projectUrl: string }>()
  return params
}
