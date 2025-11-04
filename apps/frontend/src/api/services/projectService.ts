import type {
  GetProjectListParams,
  GetProjectListResponse,
  GetProjectMembersResponse,
  GetProjectMembersParams,
} from '@/types'
import { apiClient } from '../client'
import axios from 'axios'

// 프로젝트 카드 목록 조회 (메인 페이지용)
export const getProjectList = async ({
  size,
  page,
}: GetProjectListParams): Promise<GetProjectListResponse> => {
  const url = '/api/v1/projects/info'

  try {
    const response = await apiClient.get<GetProjectListResponse>(url, {
      params: { size, page },
    })
    return response.data
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const serverErrorMessage = error.response?.data?.message
      throw new Error(serverErrorMessage || 'Failed to fetch project list')
    }
    throw new Error('Unknown error occurred')
  }
}

// 사이드바용 프로젝트 목록 조회
export const getProjectSidebar = async ({
  size,
  page,
}: GetProjectListParams): Promise<GetProjectListResponse> => {
  const url = '/api/v1/projects'

  try {
    const response = await apiClient.get<GetProjectListResponse>(url, {
      params: { size, page },
    })
    return response.data
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const serverErrorMessage = error.response?.data?.message
      throw new Error(serverErrorMessage || 'Failed to fetch project sidebar')
    }
    throw new Error('Unknown error occurred')
  }
}

//프로젝트 멤버 조회
export const getProjectMember = async ({
  projectUrl,
  size,
  page,
}: GetProjectMembersParams): Promise<GetProjectMembersResponse> => {
  const url = `/api/v1/projects/${projectUrl}/people`
  try {
    const response = await apiClient.get<GetProjectMembersResponse>(url, {
      params: {
        size,
        page,
      },
    })
    return response.data
  } catch (error) {
    console.error('Failed to fetch project members:', error)

    if (axios.isAxiosError(error)) {
      const serverErrorMessage = error.response?.data?.message
      throw new Error(serverErrorMessage || 'API 요청 중 오류가 발생했습니다.')
    } else {
      throw new Error('알 수 없는 오류가 발생했습니다.')
    }
  }
}
