import { http, HttpResponse } from 'msw'
import type { GetProjectListResponse, GetProjectMembersResponse } from '@/types'
import type { User } from '@/api/services/authService'
import type { LabelListResponse, Label } from '@/types/label'
import type {
  TemplateListItem,
  TemplateDetail,
  IssueTemplateListResponse,
  MeetingTemplateListResponse,
  RetroTemplateListResponse,
  ScrumTemplateListResponse,
} from '@/types/template'
import {
  MOCK_MEMBER_PROFILES,
  mockIssues,
  mockProjectList,
} from '@/mocks/mockTasks'

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

// Mock labels data
export const MOCK_LABELS: Label[] = [
  {
    id: 1,
    name: 'bug',
    description: '라벨에 대한 설명이 들어감',
    color: '#FF4040',
  },
  {
    id: 2,
    name: 'documentation',
    description: '라벨에 대한 설명이 들어감',
    color: '#4040FF',
  },
  {
    id: 3,
    name: 'duplicate',
    description: '라벨에 대한 설명이 들어감',
    color: '#40FF46',
  },
  {
    id: 4,
    name: 'enhancement',
    description: '라벨에 대한 설명이 들어감',
    color: '#40FFF5',
  },
  {
    id: 5,
    name: 'invalid',
    description: '라벨에 대한 설명이 들어감',
    color: '#FFE240',
  },
]

// In-memory storage for labels during tests
let labelsStore: Label[] = [...MOCK_LABELS]
let nextLabelId = 6

// Mock templates data
export const MOCK_ISSUE_TEMPLATES: TemplateListItem[] = [
  {
    templateId: 1,
    title: 'Bug Report Template',
    description: 'Template for reporting bugs',
  },
  {
    templateId: 2,
    title: 'Feature Request Template',
    description: 'Template for requesting new features',
  },
]

export const MOCK_MEETING_TEMPLATES: TemplateListItem[] = [
  {
    templateId: 3,
    title: 'Sprint Planning',
    description: 'Template for sprint planning meetings',
  },
]

export const MOCK_RETRO_TEMPLATES: TemplateListItem[] = [
  {
    templateId: 4,
    title: 'Sprint Retrospective',
    description: 'Template for sprint retrospective',
  },
]

export const MOCK_SCRUM_TEMPLATES: TemplateListItem[] = [
  {
    templateId: 5,
    title: 'Daily Standup',
    description: 'Template for daily scrum meetings',
  },
]

// In-memory storage for templates during tests
let issueTemplatesStore: TemplateListItem[] = [...MOCK_ISSUE_TEMPLATES]
let meetingTemplatesStore: TemplateListItem[] = [...MOCK_MEETING_TEMPLATES]
let retroTemplatesStore: TemplateListItem[] = [...MOCK_RETRO_TEMPLATES]
let scrumTemplatesStore: TemplateListItem[] = [...MOCK_SCRUM_TEMPLATES]
let nextTemplateId = 6

// Template details storage (for full content)
const templateDetailsStore: Record<number, TemplateDetail> = {
  1: {
    title: 'Bug Report Template',
    description: 'Template for reporting bugs',
    contents:
      '# Bug Report\n\n## Description\n\n## Steps to Reproduce\n\n## Expected Behavior\n\n## Actual Behavior',
  },
  2: {
    title: 'Feature Request Template',
    description: 'Template for requesting new features',
    contents:
      '# Feature Request\n\n## Problem Statement\n\n## Proposed Solution\n\n## Alternatives Considered',
  },
  3: {
    title: 'Sprint Planning',
    description: 'Template for sprint planning meetings',
    contents:
      '# Sprint Planning\n\n## Sprint Goal\n\n## Stories to Include\n\n## Team Capacity',
  },
  4: {
    title: 'Sprint Retrospective',
    description: 'Template for sprint retrospective',
    contents:
      '# Sprint Retrospective\n\n## What Went Well\n\n## What Could Be Improved\n\n## Action Items',
  },
  5: {
    title: 'Daily Standup',
    description: 'Template for daily scrum meetings',
    contents: '# Daily Standup\n\n## Yesterday\n\n## Today\n\n## Blockers',
  },
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

const createPutHandlers = (
  path: string,
  handler: Parameters<typeof http.put>[1]
) => [
  http.put(path, handler), // 상대 경로
  http.put(`${API_BASE_URL}${path}`, handler), // 절대 URL
]

const createDeleteHandlers = (
  path: string,
  handler: Parameters<typeof http.delete>[1]
) => [
  http.delete(path, handler), // 상대 경로
  http.delete(`${API_BASE_URL}${path}`, handler), // 절대 URL
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
          role: 'OWNER',
          description: 'Frontend developer',
        },
        {
          peopleId: 2,
          nickname: 'Bob',
          email: 'bob@example.com',
          imageUrl: 'https://placehold.co/100x100',
          role: 'MEMBER',
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

  // 프로젝트 상세내용 수정 (PUT은 GET보다 먼저 와야 함)
  ...createPutHandlers(
    '/api/v1/projects/:projectUrl',
    async ({ params, request }) => {
      const body = (await request.json()) as {
        title: string
        url: string
        summary: string
      }
      console.log('[MSW] 프로젝트 수정됨:', params.projectUrl, body)
      // 수정된 프로젝트 ID 반환
      return HttpResponse.json(123)
    }
  ),

  // 프로젝트 상세내용 조회
  ...createHandlers('/api/v1/projects/:projectUrl', ({ params }) => {
    console.log('[MSW] 프로젝트 상세 조회 호출됨:', params.projectUrl)
    const response = {
      title: 'Test Project',
      url: 'test-project',
      summary: 'Test project summary',
      imageUrl: 'https://placehold.co/600x400',
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
        role: 'OWNER',
      })
    }
  ),

  // 프로젝트별 사용자 정보 조회 (getUserInfo)
  ...createHandlers(
    '/api/v1/projects/:projectUrl/profiles/me',
    ({ params }) => {
      console.log('[MSW] 프로젝트 내 내 정보 조회:', params.projectUrl)
      // 실제 API에서는 해당 프로젝트의 내 정보를 반환
      return HttpResponse.json(MOCK_MEMBER_PROFILES)
    }
  ),

  // 특정 멤버 프로필 조회 (getMemberProfileById)
  ...createHandlers(
    '/api/v1/projects/:projectUrl/profiles/:profileId',
    ({ params }) => {
      console.log(
        '[MSW] 특정 멤버 프로필 조회:',
        params.projectUrl,
        params.profileId
      )
      // profileId에 따라 다른 Mock 데이터를 반환하거나, 여기서는 하나의 Mock 데이터를 반환
      if (Number(params.profileId) === MOCK_MEMBER_PROFILES[0].profileId) {
        return HttpResponse.json(MOCK_MEMBER_PROFILES[0])
      }
      return HttpResponse.json(MOCK_MEMBER_PROFILES[0])
    }
  ),

  // 내 프로필 수정 (updateMyProfile - PUT)
  ...createPutHandlers(
    '/api/v1/projects/:projectUrl/profiles',
    async ({ params, request }) => {
      const body = await request.json()
      console.log('[MSW] 프로젝트 내 내 프로필 수정:', params.projectUrl, body)
      // 응답 본문이 비어있는 200 OK를 가정
      return new HttpResponse(null, { status: 200 })
    }
  ),

  // 멤버 역할 수정 (updateMemberRole - PATCH)
  ...createPatchHandlers(
    '/api/v1/projects/:projectUrl/profiles/role',
    async ({ params, request }) => {
      const body = await request.json()
      console.log('[MSW] 멤버 역할 수정:', params.projectUrl, body)
      // 응답 본문이 비어있는 200 OK를 가정
      return new HttpResponse(null, { status: 200 })
    }
  ),

  // 사용자 정보 업데이트 (닉네임 변경 등)
  ...createPatchHandlers('/api/v1/users', async ({ request }) => {
    const body = (await request.json()) as { nickname: string }
    console.log('[MSW] 사용자 정보 업데이트:', body)
    return HttpResponse.json(body.nickname)
  }),

  // S3 pre-signed URL 생성
  ...createPostHandlers('/api/v1/s3/pre-signed-url', async ({ request }) => {
    const body = (await request.json()) as {
      fileName: string
      contentType: string
    }
    console.log('[MSW] S3 pre-signed URL 요청:', body)
    return HttpResponse.json({
      preSignedUrl: `https://mock-s3.amazonaws.com/bucket/${body.fileName}`,
      objectKey: `uploads/${Date.now()}/${body.fileName}`,
    })
  }),

  // S3 업로드 (외부 URL)
  http.put('https://mock-s3.amazonaws.com/bucket/*', async () => {
    console.log('[MSW] S3 파일 업로드')
    return new HttpResponse(null, { status: 200 })
  }),

  // 프로젝트 메인 이미지 업데이트
  ...createPatchHandlers(
    '/api/v1/projects/:projectUrl/image',
    async ({ params, request }) => {
      const body = (await request.json()) as { objectKey: string }
      console.log(
        '[MSW] 프로젝트 메인 이미지 업데이트:',
        params.projectUrl,
        body
      )
      return new HttpResponse(null, { status: 200 })
    }
  ),

  // 프로젝트 메인 이미지 삭제
  ...createDeleteHandlers(
    '/api/v1/projects/:projectUrl/image',
    ({ params }) => {
      console.log('[MSW] 프로젝트 메인 이미지 삭제:', params.projectUrl)
      return new HttpResponse(null, { status: 200 })
    }
  ),

  // 사용자 이미지 업데이트
  ...createPatchHandlers('/api/v1/users/image', async ({ request }) => {
    const body = (await request.json()) as { objectKey: string }
    console.log('[MSW] 사용자 이미지 업데이트:', body)
    return new HttpResponse(null, { status: 200 })
  }),

  // 사용자 이미지 삭제
  ...createDeleteHandlers('/api/v1/users/image', () => {
    console.log('[MSW] 사용자 이미지 삭제')
    return new HttpResponse(null, { status: 200 })
  }),

  // 프로젝트 내 사용자 이미지 업데이트
  ...createPatchHandlers(
    '/api/v1/projects/:projectUrl/profiles/image',
    async ({ params, request }) => {
      const body = (await request.json()) as { objectKey: string }
      console.log(
        '[MSW] 프로젝트 사용자 이미지 업데이트:',
        params.projectUrl,
        body
      )
      return new HttpResponse(null, { status: 200 })
    }
  ),

  // 프로젝트 내 사용자 이미지 삭제
  ...createDeleteHandlers(
    '/api/v1/projects/:projectUrl/profiles/image',
    ({ params }) => {
      console.log('[MSW] 프로젝트 사용자 이미지 삭제:', params.projectUrl)
      return new HttpResponse(null, { status: 200 })
    }
  ),

  // 라벨 목록 조회
  ...createHandlers('/api/v1/projects/:projectUrl/labels', ({ params }) => {
    console.log('[MSW] 라벨 목록 조회 호출됨:', params.projectUrl)
    const response: LabelListResponse = {
      labels: labelsStore,
      size: labelsStore.length,
    }
    return HttpResponse.json(response)
  }),

  // 라벨 생성
  ...createPostHandlers(
    '/api/v1/projects/:projectUrl/labels',
    async ({ params, request }) => {
      const body = (await request.json()) as {
        name: string
        description: string
        color: string
      }
      console.log('[MSW] 라벨 생성됨:', params.projectUrl, body)
      const newLabel: Label = {
        id: nextLabelId++,
        ...body,
      }
      labelsStore.push(newLabel)
      return HttpResponse.json(undefined)
    }
  ),

  // 라벨 수정
  ...createPutHandlers(
    '/api/v1/projects/:projectUrl/labels/:labelId',
    async ({ params, request }) => {
      const body = (await request.json()) as {
        name: string
        description: string
        color: string
      }
      const labelId = Number(params.labelId)
      console.log('[MSW] 라벨 수정됨:', params.projectUrl, labelId, body)

      const labelIndex = labelsStore.findIndex(label => label.id === labelId)
      if (labelIndex !== -1) {
        labelsStore[labelIndex] = {
          ...labelsStore[labelIndex],
          ...body,
        }
      }
      return HttpResponse.json(undefined)
    }
  ),

  // 라벨 삭제
  ...createDeleteHandlers(
    '/api/v1/projects/:projectUrl/labels',
    async ({ params, request }) => {
      const body = (await request.json()) as { labelId: number }
      console.log('[MSW] 라벨 삭제됨:', params.projectUrl, body.labelId)

      labelsStore = labelsStore.filter(label => label.id !== body.labelId)
      return HttpResponse.json(undefined)
    }
  ),

  // 템플릿 목록 조회 (Issues)
  ...createHandlers(
    '/api/v1/projects/:projectUrl/issues/templates',
    ({ params }) => {
      console.log('[MSW] Issue 템플릿 목록 조회 호출됨:', params.projectUrl)
      const response: IssueTemplateListResponse = {
        issueTemplates: issueTemplatesStore,
        size: issueTemplatesStore.length,
      }
      return HttpResponse.json(response)
    }
  ),

  // 템플릿 목록 조회 (Meetings)
  ...createHandlers(
    '/api/v1/projects/:projectUrl/meetings/templates',
    ({ params }) => {
      console.log('[MSW] Meeting 템플릿 목록 조회 호출됨:', params.projectUrl)
      const response: MeetingTemplateListResponse = {
        meetingTemplates: meetingTemplatesStore,
        size: meetingTemplatesStore.length,
      }
      return HttpResponse.json(response)
    }
  ),

  // 템플릿 목록 조회 (Retros)
  ...createHandlers(
    '/api/v1/projects/:projectUrl/retros/templates',
    ({ params }) => {
      console.log('[MSW] Retro 템플릿 목록 조회 호출됨:', params.projectUrl)
      const response: RetroTemplateListResponse = {
        retroTemplates: retroTemplatesStore,
        size: retroTemplatesStore.length,
      }
      return HttpResponse.json(response)
    }
  ),

  // 템플릿 목록 조회 (Scrums)
  ...createHandlers(
    '/api/v1/projects/:projectUrl/scrums/templates',
    ({ params }) => {
      console.log('[MSW] Scrum 템플릿 목록 조회 호출됨:', params.projectUrl)
      const response: ScrumTemplateListResponse = {
        scrumTemplates: scrumTemplatesStore,
        size: scrumTemplatesStore.length,
      }
      return HttpResponse.json(response)
    }
  ),

  // 템플릿 상세 조회 (모든 리소스 타입)
  ...createHandlers(
    '/api/v1/projects/:projectUrl/:resourceType/templates/:templateId',
    ({ params }) => {
      const templateId = Number(params.templateId)
      console.log(
        '[MSW] 템플릿 상세 조회 호출됨:',
        params.projectUrl,
        params.resourceType,
        templateId
      )
      const detail = templateDetailsStore[templateId]
      if (detail) {
        return HttpResponse.json(detail)
      }
      return HttpResponse.json({ error: 'Template not found' }, { status: 404 })
    }
  ),

  // 템플릿 생성 (Issues)
  ...createPostHandlers(
    '/api/v1/projects/:projectUrl/issues/templates',
    async ({ params, request }) => {
      const body = (await request.json()) as TemplateDetail
      console.log('[MSW] Issue 템플릿 생성됨:', params.projectUrl, body)
      const newTemplate: TemplateListItem = {
        templateId: nextTemplateId,
        title: body.title,
        description: body.description,
      }
      issueTemplatesStore.push(newTemplate)
      templateDetailsStore[nextTemplateId] = body
      nextTemplateId++
      return HttpResponse.json(undefined)
    }
  ),

  // 템플릿 생성 (Meetings)
  ...createPostHandlers(
    '/api/v1/projects/:projectUrl/meetings/templates',
    async ({ params, request }) => {
      const body = (await request.json()) as TemplateDetail
      console.log('[MSW] Meeting 템플릿 생성됨:', params.projectUrl, body)
      const newTemplate: TemplateListItem = {
        templateId: nextTemplateId,
        title: body.title,
        description: body.description,
      }
      meetingTemplatesStore.push(newTemplate)
      templateDetailsStore[nextTemplateId] = body
      nextTemplateId++
      return HttpResponse.json(undefined)
    }
  ),

  // 템플릿 생성 (Retros)
  ...createPostHandlers(
    '/api/v1/projects/:projectUrl/retros/templates',
    async ({ params, request }) => {
      const body = (await request.json()) as TemplateDetail
      console.log('[MSW] Retro 템플릿 생성됨:', params.projectUrl, body)
      const newTemplate: TemplateListItem = {
        templateId: nextTemplateId,
        title: body.title,
        description: body.description,
      }
      retroTemplatesStore.push(newTemplate)
      templateDetailsStore[nextTemplateId] = body
      nextTemplateId++
      return HttpResponse.json(undefined)
    }
  ),

  // 템플릿 생성 (Scrums)
  ...createPostHandlers(
    '/api/v1/projects/:projectUrl/scrums/templates',
    async ({ params, request }) => {
      const body = (await request.json()) as TemplateDetail
      console.log('[MSW] Scrum 템플릿 생성됨:', params.projectUrl, body)
      const newTemplate: TemplateListItem = {
        templateId: nextTemplateId,
        title: body.title,
        description: body.description,
      }
      scrumTemplatesStore.push(newTemplate)
      templateDetailsStore[nextTemplateId] = body
      nextTemplateId++
      return HttpResponse.json(undefined)
    }
  ),

  // 템플릿 수정 (모든 리소스 타입)
  ...createPutHandlers(
    '/api/v1/projects/:projectUrl/:resourceType/templates',
    async ({ params, request }) => {
      const body = (await request.json()) as {
        templateId: number
        title: string
        description: string
        contents: string
      }
      console.log(
        '[MSW] 템플릿 수정됨:',
        params.projectUrl,
        params.resourceType,
        body.templateId
      )

      // Update in appropriate store based on resource type
      const resourceType = params.resourceType as string
      let store: TemplateListItem[]
      if (resourceType === 'issues') store = issueTemplatesStore
      else if (resourceType === 'meetings') store = meetingTemplatesStore
      else if (resourceType === 'retros') store = retroTemplatesStore
      else store = scrumTemplatesStore

      const templateIndex = store.findIndex(
        t => t.templateId === body.templateId
      )
      if (templateIndex !== -1) {
        store[templateIndex] = {
          templateId: body.templateId,
          title: body.title,
          description: body.description,
        }
        templateDetailsStore[body.templateId] = {
          title: body.title,
          description: body.description,
          contents: body.contents,
        }
      }
      return HttpResponse.json(undefined)
    }
  ),

  // 템플릿 삭제 (모든 리소스 타입)
  ...createDeleteHandlers(
    '/api/v1/projects/:projectUrl/:resourceType/templates',
    async ({ params, request }) => {
      const body = (await request.json()) as { templateId: number }
      console.log(
        '[MSW] 템플릿 삭제됨:',
        params.projectUrl,
        params.resourceType,
        body.templateId
      )

      // Delete from appropriate store based on resource type
      const resourceType = params.resourceType as string
      if (resourceType === 'issues') {
        issueTemplatesStore = issueTemplatesStore.filter(
          t => t.templateId !== body.templateId
        )
      } else if (resourceType === 'meetings') {
        meetingTemplatesStore = meetingTemplatesStore.filter(
          t => t.templateId !== body.templateId
        )
      } else if (resourceType === 'retros') {
        retroTemplatesStore = retroTemplatesStore.filter(
          t => t.templateId !== body.templateId
        )
      } else {
        scrumTemplatesStore = scrumTemplatesStore.filter(
          t => t.templateId !== body.templateId
        )
      }
      delete templateDetailsStore[body.templateId]
      return HttpResponse.json(undefined)
    }
  ),
]

// Helper function to reset labels store for tests
export const resetLabelsStore = () => {
  labelsStore = [...MOCK_LABELS]
  nextLabelId = 6
}

// Helper function to reset templates store for tests
export const resetTemplatesStore = () => {
  issueTemplatesStore = [...MOCK_ISSUE_TEMPLATES]
  meetingTemplatesStore = [...MOCK_MEETING_TEMPLATES]
  retroTemplatesStore = [...MOCK_RETRO_TEMPLATES]
  scrumTemplatesStore = [...MOCK_SCRUM_TEMPLATES]
  nextTemplateId = 6
}
