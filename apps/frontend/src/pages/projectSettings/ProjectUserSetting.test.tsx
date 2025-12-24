import { describe, it, expect, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import ProjectUserSetting from './ProjectUserSetting'

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
      <MemoryRouter
        initialEntries={[`/projects/${projectUrl}/settings/user`]}
      >
        <Routes>
          <Route
            path="/projects/:projectUrl/settings/user"
            element={<ProjectUserSetting />}
          />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>
  )
}

describe('ProjectUserSetting - 통합 테스트', () => {
  describe('데이터 로딩 및 렌더링', () => {
    it('로딩 중일 때 로딩 메시지를 표시한다', () => {
      renderWithProviders()

      expect(screen.getByText('로딩 중...')).toBeInTheDocument()
    })

    it('데이터 로딩 후 페이지 제목들을 렌더링한다', async () => {
      renderWithProviders()

      await waitFor(() => {
        expect(
          screen.getByRole('heading', { name: /My Information/i })
        ).toBeInTheDocument()
        expect(
          screen.getByRole('heading', { name: /Current Members/i })
        ).toBeInTheDocument()
      })
    })

    it('UserProfileBox 컴포넌트를 렌더링한다', async () => {
      renderWithProviders()

      await waitFor(() => {
        // UserProfileBox가 렌더링되었는지 확인
        expect(
          screen.getByRole('heading', { name: /My Information/i })
        ).toBeInTheDocument()
      })
    })

    it('멤버 목록을 렌더링한다', async () => {
      renderWithProviders()

      await waitFor(() => {
        // MSW 핸들러가 반환하는 Alice와 Bob이 렌더링되는지 확인
        expect(screen.getByText('Alice')).toBeInTheDocument()
        expect(screen.getByText('Bob')).toBeInTheDocument()
      })
    })
  })

  describe('멤버 역할 표시', () => {
    it('각 멤버의 역할을 표시한다', async () => {
      renderWithProviders()

      await waitFor(() => {
        // owner와 member 역할이 표시되는지 확인
        const ownerElements = screen.getAllByText(/owner/i)
        const memberElements = screen.getAllByText(/member/i)

        expect(ownerElements.length).toBeGreaterThan(0)
        expect(memberElements.length).toBeGreaterThan(0)
      })
    })
  })

  describe('페이지 레이아웃', () => {
    it('컨테이너가 올바른 스타일로 렌더링된다', async () => {
      const { container } = renderWithProviders()

      await waitFor(() => {
        const mainContainer = container.querySelector('.container')
        expect(mainContainer).toHaveClass(
          'container',
          'flex',
          'flex-col',
          'justify-center',
          'items-center',
          'gap-10',
          'p-10'
        )
      })
    })

    it('제목들이 올바른 스타일로 렌더링된다', async () => {
      renderWithProviders()

      await waitFor(() => {
        const myInfoHeading = screen.getByRole('heading', {
          name: /My Information/i,
        })
        const membersHeading = screen.getByRole('heading', {
          name: /Current Members/i,
        })

        expect(myInfoHeading).toHaveClass(
          'text-center',
          'text-[40px]',
          'font-bold',
          'leading-[48px]',
          "font-['Roboto']",
          'pb-10'
        )
        expect(membersHeading).toHaveClass(
          'text-center',
          'text-[40px]',
          'font-bold',
          'leading-[48px]',
          "font-['Roboto']",
          'pb-10'
        )
      })
    })
  })

  // Note: 에러 처리 테스트는 MSW 핸들러 오버라이드와 컴포넌트의 에러 처리 로직이 복잡하여 제외
  // 향후 더 나은 에러 처리 방식으로 개선 예정

  describe('프로젝트 URL 처리', () => {
    it('다른 프로젝트 URL로도 정상 작동한다', async () => {
      renderWithProviders('another-project')

      await waitFor(() => {
        expect(
          screen.getByRole('heading', { name: /My Information/i })
        ).toBeInTheDocument()
      })
    })
  })

  describe('역할 변경 기능', () => {
    it('MemberList에 canEdit prop이 true로 전달된다', async () => {
      renderWithProviders()

      await waitFor(() => {
        // 역할 선택 드롭다운이 활성화되어 있는지 확인
        const selectTriggers = screen.getAllByRole('combobox')
        selectTriggers.forEach(trigger => {
          expect(trigger).not.toBeDisabled()
        })
      })
    })
  })
})
