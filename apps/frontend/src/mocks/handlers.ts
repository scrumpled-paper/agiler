import { http, HttpResponse } from 'msw'
import type { GetProjectListResponse, GetProjectMembersResponse } from '@/types'

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://43.200.4.72:8080'

export const handlers = [
  // 프로젝트 목록 조회 (메인 페이지용)
  http.get(`${API_BASE_URL}/api/v1/projects/info`, ({ request }) => {
    const url = new URL(request.url)
    const page = Number(url.searchParams.get('page')) || 0
    const size = Number(url.searchParams.get('size')) || 6

    const response: GetProjectListResponse = {
      contents: [
        {
          title: 'Agile Project',
          url: '/projects/agile-project',
          imageUrl: 'https://placehold.co/600x400',
          summary: 'An agile project management tool',
        },
        {
          title: 'Design System',
          url: '/projects/design-system',
          imageUrl: 'https://placehold.co/600x400',
          summary: 'Company-wide design system',
        },
      ],
      totalPages: 1,
      totalElements: 2,
      currentPage: page,
      pageSize: size,
    }

    return HttpResponse.json(response)
  }),

  // 사이드바용 프로젝트 목록 조회
  http.get(`${API_BASE_URL}/api/v1/projects`, () => {
    const response: GetProjectListResponse = {
      contents: [
        {
          title: 'Agile Project',
          url: '/projects/agile-project',
          imageUrl: 'https://placehold.co/600x400',
          summary: 'An agile project management tool',
        },
      ],
      totalPages: 1,
      totalElements: 1,
      currentPage: 0,
      pageSize: 10,
    }

    return HttpResponse.json(response)
  }),

  // 프로젝트 멤버 조회
  http.get(`${API_BASE_URL}/api/v1/projects/:projectUrl/people`, () => {
    const response: GetProjectMembersResponse = {
      contents: [
        {
          peopleId: 1,
          nickname: 'Alice',
          email: 'alice@example.com',
          imageUrl: 'https://placehold.co/100x100',
          role: 'Developer',
          description: 'Frontend developer',
        },
        {
          peopleId: 2,
          nickname: 'Bob',
          email: 'bob@example.com',
          imageUrl: 'https://placehold.co/100x100',
          role: 'Designer',
          description: 'UI/UX designer',
        },
      ],
      totalPages: 1,
      number: 0,
      size: 5,
    }

    return HttpResponse.json(response)
  }),

  // 프로젝트 URL 검증
  http.get(
    `${API_BASE_URL}/api/v1/projects/check/:projectUrl`,
    ({ params }) => {
      const { projectUrl } = params
      // 'existing-project'는 이미 존재하는 것으로 처리
      const isAvailable = projectUrl !== 'existing-project'
      return HttpResponse.json(isAvailable)
    }
  ),

  // 프로젝트 생성
  http.post(`${API_BASE_URL}/api/v1/projects`, async ({ request }) => {
    const body = (await request.json()) as {
      title: string
      url: string
      summary: string
    }
    console.log('test 프로젝트 생성됨 : ', body)
    // 생성된 프로젝트 ID 반환
    return HttpResponse.json(123)
  }),
]
