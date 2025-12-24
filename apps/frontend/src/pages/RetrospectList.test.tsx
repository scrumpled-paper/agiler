import { describe, it, expect } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import RetrospectivesList from './RetrospectList'

/**
 * 테스트용 래퍼 컴포넌트
 * - MemoryRouter: useParams를 사용할 수 있도록 routing context 제공
 * - QueryClientProvider: useQuery를 사용할 수 있도록 React Query context 제공
 */
function renderWithProviders(projectUrl = 'test-project') {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false, // 테스트에서는 재시도 비활성화
      },
    },
  })

  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[`/projects/${projectUrl}/retrospectives`]}>
        <Routes>
          <Route
            path="/projects/:projectUrl/retrospectives"
            element={<RetrospectivesList />}
          />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>
  )
}

describe('RetrospectivesList - 통합 테스트', () => {
  describe('데이터 로딩 및 렌더링', () => {
    it('로딩 중일 때 로딩 메시지를 표시한다', () => {
      renderWithProviders()

      expect(screen.getByText('로딩 중...')).toBeInTheDocument()
    })

    it('데이터 로딩 후 페이지 제목을 렌더링한다', async () => {
      renderWithProviders()

      await waitFor(() => {
        expect(
          screen.getByRole('heading', { name: /회고 목록/i })
        ).toBeInTheDocument()
      })
    })

    it('MSW를 통해 받은 데이터를 ContentListTable에 렌더링한다', async () => {
      renderWithProviders()

      // MSW 핸들러가 10개의 회고 항목을 반환하므로, 첫 번째 항목이 렌더링되는지 확인
      await waitFor(() => {
        expect(screen.getByText('회고 #1')).toBeInTheDocument()
      })
    })

    it('참여자 정보를 렌더링한다', async () => {
      renderWithProviders()

      await waitFor(() => {
        // 참여자 아바타의 fallback 텍스트가 렌더링되는지 확인
        // Alice의 첫 글자 'A', Bob의 첫 글자 'B'
        const avatarFallbacks = screen.getAllByText('A')
        expect(avatarFallbacks.length).toBeGreaterThan(0)
      })
    })
  })

  describe('페이지 스타일링', () => {
    it('제목이 올바른 스타일로 렌더링된다', async () => {
      renderWithProviders()

      await waitFor(() => {
        const heading = screen.getByRole('heading', {
          name: /회고 목록/i,
        })
        expect(heading).toHaveClass('text-3xl', 'font-bold', 'mb-4')
      })
    })

    it('컨테이너가 올바른 스타일로 렌더링된다', async () => {
      const { container } = renderWithProviders()

      await waitFor(() => {
        const containerDiv = container.querySelector('.container.p-4')
        expect(containerDiv).toBeInTheDocument()
      })
    })
  })

  describe('프로젝트 URL 처리', () => {
    it('다른 프로젝트 URL로도 정상 작동한다', async () => {
      renderWithProviders('another-project')

      await waitFor(() => {
        expect(
          screen.getByRole('heading', { name: /회고 목록/i })
        ).toBeInTheDocument()
      })
    })
  })
})
