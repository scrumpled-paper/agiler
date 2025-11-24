import { http, HttpResponse } from 'msw'
import type { GetProjectListResponse, GetProjectMembersResponse } from '@/types'
import type { User } from '@/api/services/authService'
import { mockIssues, mockProjectList } from '@/mocks/mockTasks'

// MSW 핸들러: 상대 경로와 절대 URL 모두 매칭
// - 개발: /api/v1/... (Vite 프록시)
// - 테스트/프로덕션: https://agiler.p-e.kr/api/v1/...
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'https://agiler.p-e.kr'

export const MOCK_USER: User = {
  id: 1,
  email: 'test@agiler.com',
  nickname: 'AgileTester',
  imageUrl: 'https://via.placeholder.com/150',
}

// 각 경로에 대해 상대/절대 핸들러 모두 생성
const createHandlers = (
  path: string,
  handler: Parameters<typeof http.get>[1]
) => [
  http.get(path, handler), // 상대 경로
  http.get(`${API_BASE_URL}${path}`, handler), // 절대 URL
]

const createPostHandlers = (
  path: string,
  handler: Parameters<typeof http.post>[1]
) => [
  http.post(path, handler), // 상대 경로
  http.post(`${API_BASE_URL}${path}`, handler), // 절대 URL
]

const createPatchHandlers = (
  path: string,
  handler: Parameters<typeof http.patch>[1]
) => [
  http.patch(path, handler), // 상대 경로
  http.patch(`${API_BASE_URL}${path}`, handler), // 절대 URL
]

export const handlers = [
  // 현재 사용자 정보 조회
  ...createHandlers('/api/v1/users', () => {
    return HttpResponse.json(MOCK_USER)
  }),

  // 로그아웃
  ...createPostHandlers('/api/v1/logout', () => {
    return HttpResponse.json({ message: 'Logged out successfully' })
  }),

  // 프로젝트 목록 조회 (메인 페이지용)
  ...createHandlers('/api/v1/projects/info', ({ request }) => {
    const url = new URL(request.url)
    const page = Number(url.searchParams.get('page')) || 0
    const size = Number(url.searchParams.get('size')) || 6

    const response: GetProjectListResponse = {
      contents: mockProjectList,
      totalPages: 1,
      totalElements: 2,
      currentPage: page,
      pageSize: size,
    }

    return HttpResponse.json(response)
  }),

  // 사이드바용 프로젝트 목록 조회
  ...createHandlers('/api/v1/projects', () => {
    const response: GetProjectListResponse = {
      contents: mockProjectList,
      totalPages: 1,
      totalElements: 2,
      currentPage: 0,
      pageSize: 10,
    }

    return HttpResponse.json(response)
  }),

  // 프로젝트 멤버 조회
  ...createHandlers('/api/v1/projects/:projectUrl/profiles', ({ params }) => {
    console.log('[MSW] 프로젝트 멤버 조회 호출됨:', params.projectUrl)
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

  // 프로젝트 URL 검증 (쿼리 파라미터 방식)
  ...createHandlers('/api/v1/projects/check', ({ request }) => {
    const url = new URL(request.url)
    const projectUrl = url.searchParams.get('url')
    // 'existing-project'는 이미 존재하는 것으로 처리
    const isAvailable = projectUrl !== 'existing-project'
    return HttpResponse.json(isAvailable)
  }),

  // 프로젝트 생성
  ...createPostHandlers('/api/v1/projects', async ({ request }) => {
    const body = (await request.json()) as {
      title: string
      url: string
      summary: string
    }
    console.log('test 프로젝트 생성됨 : ', body)
    // 생성된 프로젝트 ID 반환
    return HttpResponse.json(123)
  }),

  // 프로젝트 상세내용 조회
  ...createHandlers('/api/v1/projects/:projectUrl', () => {
    console.log('[MSW] 프로젝트 상세 조회 호출됨:')
    const response = {
      contents: mockIssues,
      size: mockIssues.length,
    }
    return HttpResponse.json(response)
  }),
  // 칸반 정보 조회
  ...createHandlers('/api/v1/projects/:projectUrl/kanban', ({ params }) => {
    console.log('[MSW] 프로젝트 칸반 조회 호출됨:', params.projectUrl)
    const response = {
      contents: mockIssues,
      size: mockIssues.length,
    }
    return HttpResponse.json(response)
  }),

  // 칸반 이슈 업데이트 (드래그 앤 드롭)
  ...createPatchHandlers(
    '/api/v1/projects/:projectUrl/kanban/:issueId',
    async ({ params, request }) => {
      console.log(
        '[MSW] 칸반 이슈 업데이트 호출됨:',
        params.projectUrl,
        params.issueId
      )
      const body = await request.json()
      console.log('[MSW] 업데이트 데이터:', body)
      return HttpResponse.json({ success: true })
    }
  ),

  // 프로젝트별 사용자 정보 조회
  ...createHandlers(
    '/api/v1/projects/:projectUrl/profiles/me',
    ({ params }) => {
      console.log('[MSW] 프로젝트 사용자 정보 조회:', params.projectUrl)
      return HttpResponse.json({
        profileId: 1,
        nickname: 'Project User',
        email: 'project@agiler.com',
        imageUrl: 'https://via.placeholder.com/150',
        role: 'Owner',
      })
    }
  ),

  // 사용자 정보 업데이트 (닉네임 변경 등)
  ...createPatchHandlers('/api/v1/users', async ({ request }) => {
    const body = (await request.json()) as { nickname: string }
    console.log('[MSW] 사용자 정보 업데이트:', body)
    return HttpResponse.json(body.nickname)
  }),
]
