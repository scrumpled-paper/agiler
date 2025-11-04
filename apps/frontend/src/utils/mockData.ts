import type {
  ContentItem,
  GetProjectMembersParams,
  GetProjectMembersResponse,
  PaginatedContentResponse,
  ProjectMember,
} from '@/types'

// 더미 데이터 템플릿
const contentTemplates = [
  {
    title: 'React 훅(Hooks) 완벽 가이드',
    category: 'React',
    color: '61DAFB',
    summary:
      'useState, useEffect부터 커스텀 훅까지 React 훅의 모든 것을 깊이 있게 다룹니다.',
  },
  {
    title: 'TypeScript 마스터하기',
    category: 'TypeScript',
    color: '3178C6',
    summary:
      'TypeScript의 고급 기능과 타입 시스템을 완벽하게 이해하는 방법을 배웁니다.',
  },
  {
    title: 'Next.js 14 새로운 기능',
    category: 'Next.js',
    color: '000000',
    summary:
      'App Router, Server Components 등 Next.js 14의 혁신적인 기능들을 살펴봅니다.',
  },
  {
    title: '웹 성능 최적화 기법',
    category: 'Performance',
    color: 'F0DB4F',
    summary:
      'LCP, FID, CLS 등 Core Web Vitals를 개선하는 실전 기법을 소개합니다.',
  },
  {
    title: 'Tailwind CSS 실전 활용',
    category: 'CSS',
    color: '06B6D4',
    summary:
      '유틸리티 우선 접근 방식으로 빠르고 일관된 UI를 구축하는 방법을 알아봅니다.',
  },
  {
    title: 'JavaScript ES2024 신기능',
    category: 'JavaScript',
    color: 'F7DF1E',
    summary: '최신 ECMAScript 표준의 새로운 기능들과 활용 방법을 다룹니다.',
  },
  {
    title: 'GraphQL과 Apollo Client',
    category: 'GraphQL',
    color: 'E10098',
    summary:
      'GraphQL을 활용한 효율적인 데이터 페칭과 상태 관리 방법을 배웁니다.',
  },
  {
    title: 'Node.js 백엔드 개발',
    category: 'Node.js',
    color: '339933',
    summary:
      'Express와 NestJS를 활용한 확장 가능한 백엔드 아키텍처를 구축합니다.',
  },
  {
    title: 'MongoDB 데이터베이스 설계',
    category: 'Database',
    color: '47A248',
    summary: 'NoSQL 데이터베이스 설계 원칙과 MongoDB 최적화 기법을 학습합니다.',
  },
  {
    title: 'Docker 컨테이너 활용',
    category: 'DevOps',
    color: '2496ED',
    summary: 'Docker를 활용한 개발 환경 구성과 배포 자동화 방법을 알아봅니다.',
  },
]

// Mock 데이터 생성 함수
export function generateMockContents(count: number): ContentItem[] {
  const contents: ContentItem[] = []

  for (let i = 1; i <= count; i++) {
    const template = contentTemplates[i % contentTemplates.length]
    contents.push({
      title: `${template.title} #${i}`,
      url: `/projects/${i}`,
      imageUrl: `https://placehold.co/600x400/${template.color}/FFFFFF?text=${encodeURIComponent(template.category)}+${i}`,
      summary: `${template.summary} (Article ${i})`,
    })
  }

  return contents
}

// 페이지네이션된 데이터 가져오기 (실제 API 호출을 시뮬레이션)
export function getMockPaginatedContents(
  page: number,
  pageSize: number = 6
): PaginatedContentResponse {
  // 전체 데이터 생성 (총 50개)
  const totalItems = 50
  const allContents = generateMockContents(totalItems)

  // 총 페이지 수 계산
  const totalPages = Math.ceil(totalItems / pageSize)

  // 현재 페이지의 시작/끝 인덱스 계산
  const startIndex = (page - 1) * pageSize
  const endIndex = startIndex + pageSize

  // 현재 페이지에 해당하는 데이터만 추출
  const contents = allContents.slice(startIndex, endIndex)

  return {
    contents,
    pageSize,
    currentPage: page,
    totalPages,
    totalItems,
  }
}

// API 호출을 시뮬레이션하는 비동기 함수
export async function fetchMockContents(
  page: number,
  pageSize: number = 6,
  delay: number = 500
): Promise<PaginatedContentResponse> {
  // 네트워크 지연 시뮬레이션
  await new Promise(resolve => setTimeout(resolve, delay))

  return getMockPaginatedContents(page, pageSize)
}

const memberTemplates = [
  {
    nickname: 'Alex',
    role: 'Frontend Lead',
    description: 'React와 TypeScript를 담당합니다.',
    color: '61DAFB', // React Blue
    textColor: '000000',
  },
  {
    nickname: 'Sarah',
    role: 'Backend Lead',
    description: 'Node.js API 및 데이터베이스 아키텍처를 설계합니다.',
    color: '339933', // Node Green
    textColor: 'FFFFFF',
  },
  {
    nickname: 'Chris',
    role: 'UI/UX Designer',
    description: 'Figma를 사용한 프로토타이핑 및 사용자 경험을 설계합니다.',
    color: 'A259FF', // Figma Purple
    textColor: 'FFFFFF',
  },
  {
    nickname: 'Morgan',
    role: 'Project Manager',
    description: '애자일 스프린트와 일정 관리를 총괄합니다.',
    color: 'F24E1E', // Figma Red/Orange
    textColor: 'FFFFFF',
  },
  {
    nickname: 'Jamie',
    role: 'Junior Developer',
    description: '컴포넌트 개발 및 버그 수정을 지원합니다.',
    color: 'F7DF1E', // JS Yellow
    textColor: '000000',
  },
  {
    nickname: 'Taylor',
    role: 'DevOps Engineer',
    description: 'CI/CD 파이프라인과 인프라를 관리합니다.',
    color: '2496ED', // Docker Blue
    textColor: 'FFFFFF',
  },
]
export function generateMockProjectMembers(count: number): ProjectMember[] {
  const members: ProjectMember[] = []

  for (let i = 1; i <= count; i++) {
    const template = memberTemplates[i % memberTemplates.length]
    const nickname = `${template.nickname} ${i}`
    const initials = template.nickname.substring(0, 2).toUpperCase()

    members.push({
      peopleId: i,
      nickname: nickname,
      email: `${template.nickname.toLowerCase()}${i}@example.com`,
      // placehold.co를 사용하여 이니셜 기반의 프로필 이미지 생성
      imageUrl: `https://placehold.co/100x100/${template.color}/${template.textColor}?text=${initials}&font=Inter`,
      role: template.role,
      description: `${template.description} (ID: ${i})`,
    })
  }
  return members
}

function getMockPaginatedMembers(
  pageNumber: number,
  pageSize: number
): GetProjectMembersResponse {
  // 1. 전체 데이터 생성 (총 27명의 멤버가 있다고 가정)
  const totalItems = 27
  const allMembers = generateMockProjectMembers(totalItems)

  // 2. 총 페이지 수 계산
  const totalPages = Math.ceil(totalItems / pageSize)

  // 3. 현재 페이지의 시작/끝 인덱스 계산 (0-indexed 기준)
  const startIndex = pageNumber * pageSize
  const endIndex = startIndex + pageSize

  // 4. 현재 페이지에 해당하는 데이터만 추출
  const contents = allMembers.slice(startIndex, endIndex)

  // 5. API 응답 DTO에 맞춰 반환
  return {
    contents,
    size: contents.length, // 실제 반환되는 항목 수
    number: pageNumber, // 요청한 페이지 번호
    totalPages,
  }
}

export async function fetchMockProjectMembers(
  { projectUrl, size, page }: GetProjectMembersParams,
  delay: number = 500
): Promise<GetProjectMembersResponse> {
  // 1. 네트워크 지연 시뮬레이션
  await new Promise(resolve => setTimeout(resolve, delay))

  // 2. 로그 (디버깅용)
  console.log(
    `[Mock API] Fetching members for project ${projectUrl} | Page: ${page}, Size: ${size}`
  )

  // 3. 페이지네이션된 데이터 반환
  return getMockPaginatedMembers(page, size)
}
