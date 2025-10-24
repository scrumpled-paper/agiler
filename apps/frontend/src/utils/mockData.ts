export interface ContentItem {
  title: string
  url: string
  imageUrl: string
  summary: string
}

export interface PaginatedContentResponse {
  contents: ContentItem[]
  pageSize: number
  currentPage: number
  totalPages: number
  totalItems: number
}

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
      url: `https://example.com/blog/${template.category.toLowerCase()}-${i}`,
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
