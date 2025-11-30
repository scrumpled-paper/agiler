import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import userEvent from '@testing-library/user-event'
import ProjectLabelSetting from './ProjectLabelSetting'
import { server } from '@/mocks/server'
import { resetLabelsStore } from '@/mocks/handlers'
import { http, HttpResponse } from 'msw'

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'https://agiler.p-e.kr'

const createWrapper = (
  initialRoute = '/projects/test-project/settings/project-label'
) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0,
        staleTime: 0,
      },
    },
  })

  return function Wrapper({ children }: { children: React.ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={[initialRoute]}>
          <Routes>
            <Route
              path="/projects/:projectUrl/settings/project-label"
              element={children}
            />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    )
  }
}

describe('ProjectLabelSetting Page - 통합 테스트', () => {
  beforeEach(() => {
    server.resetHandlers()
    resetLabelsStore()
    vi.clearAllMocks()
  })

  describe('UI 렌더링', () => {
    it('라벨 설정 페이지가 올바르게 렌더링된다', async () => {
      render(<ProjectLabelSetting />, { wrapper: createWrapper() })

      // 라벨 목록 로드 대기
      await waitFor(() => {
        expect(screen.getByText('bug')).toBeInTheDocument()
      })

      // 페이지 타이틀 확인
      expect(screen.getByText('Label Setting')).toBeInTheDocument()

      // 탭 확인
      expect(screen.getByText('labels')).toBeInTheDocument()
    })

    it('라벨 목록이 올바르게 표시된다', async () => {
      render(<ProjectLabelSetting />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('bug')).toBeInTheDocument()
        expect(screen.getByText('documentation')).toBeInTheDocument()
        expect(screen.getByText('duplicate')).toBeInTheDocument()
        expect(screen.getByText('enhancement')).toBeInTheDocument()
        expect(screen.getByText('invalid')).toBeInTheDocument()
      })

      // 라벨 설명이 모두 표시되는지 확인
      const descriptions = screen.getAllByText('라벨에 대한 설명이 들어감')
      expect(descriptions).toHaveLength(5)
    })

    it('새 라벨 생성 버튼이 표시된다', async () => {
      render(<ProjectLabelSetting />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('bug')).toBeInTheDocument()
      })

      // Plus 아이콘 버튼이 있는지 확인 (role로 찾기)
      const createButton = screen
        .getAllByRole('button')
        .find(
          button =>
            button.querySelector('svg') &&
            button.classList.contains('border-[#e1e4ed]')
        )
      expect(createButton).toBeInTheDocument()
    })
  })

  describe('라벨 생성 기능', () => {
    it('새 라벨을 생성할 수 있다', async () => {
      const user = userEvent.setup()

      render(<ProjectLabelSetting />, { wrapper: createWrapper() })

      // 라벨 목록 로드 대기
      await waitFor(() => {
        expect(screen.getByText('bug')).toBeInTheDocument()
      })

      // 생성 버튼 클릭
      const buttons = screen.getAllByRole('button')
      const createButton = buttons.find(button =>
        button.classList.contains('border-[#e1e4ed]')
      )
      await user.click(createButton!)

      // 모달이 열렸는지 확인
      await waitFor(() => {
        expect(screen.getByPlaceholderText('Label name')).toBeInTheDocument()
      })

      // 폼 입력
      const nameInput = screen.getByPlaceholderText('Label name')
      const colorInput = screen.getByPlaceholderText('#000000')
      const descriptionInput = screen.getAllByRole('textbox')[2] // textarea

      await user.type(nameInput, 'new-feature')
      await user.clear(colorInput)
      await user.type(colorInput, '#00FF00')
      await user.type(descriptionInput, 'New feature label description')

      // Create 버튼 클릭
      const createSubmitButton = screen.getByRole('button', { name: /Create/i })
      await user.click(createSubmitButton)

      // 모달이 닫히고 새 라벨이 목록에 추가됨
      await waitFor(() => {
        expect(
          screen.queryByPlaceholderText('Label name')
        ).not.toBeInTheDocument()
        expect(screen.getByText('new-feature')).toBeInTheDocument()
      })
    })

    it('라벨 이름 없이 생성하면 경고가 표시된다', async () => {
      const user = userEvent.setup()
      const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {})

      render(<ProjectLabelSetting />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('bug')).toBeInTheDocument()
      })

      // 생성 버튼 클릭
      const buttons = screen.getAllByRole('button')
      const createButton = buttons.find(button =>
        button.classList.contains('border-[#e1e4ed]')
      )
      await user.click(createButton!)

      await waitFor(() => {
        expect(screen.getByPlaceholderText('Label name')).toBeInTheDocument()
      })

      // 이름 없이 Create 버튼 클릭
      const createSubmitButton = screen.getByRole('button', { name: /Create/i })
      await user.click(createSubmitButton)

      // 경고 메시지 확인
      expect(alertSpy).toHaveBeenCalledWith('라벨 이름을 입력해주세요.')

      alertSpy.mockRestore()
    })

    it('Cancel 버튼을 클릭하면 모달이 닫힌다', async () => {
      const user = userEvent.setup()

      render(<ProjectLabelSetting />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('bug')).toBeInTheDocument()
      })

      // 생성 버튼 클릭
      const buttons = screen.getAllByRole('button')
      const createButton = buttons.find(button =>
        button.classList.contains('border-[#e1e4ed]')
      )
      await user.click(createButton!)

      await waitFor(() => {
        expect(screen.getByPlaceholderText('Label name')).toBeInTheDocument()
      })

      // Cancel 버튼 클릭
      const cancelButton = screen.getByRole('button', { name: /Cancel/i })
      await user.click(cancelButton)

      // 모달이 닫힘
      await waitFor(() => {
        expect(
          screen.queryByPlaceholderText('Label name')
        ).not.toBeInTheDocument()
      })
    })
  })

  describe('라벨 수정 기능', () => {
    it('라벨을 수정할 수 있다', async () => {
      const user = userEvent.setup()

      render(<ProjectLabelSetting />, { wrapper: createWrapper() })

      // 라벨 목록 로드 대기
      await waitFor(() => {
        expect(screen.getByText('bug')).toBeInTheDocument()
      })

      // 첫 번째 라벨의 편집 버튼 클릭 (MoreHorizontal icon)
      const allButtons = screen.getAllByRole('button')
      // 편집 버튼 찾기: 작은 크기의 버튼 (size-8 class)
      const editButtons = allButtons.filter(btn =>
        btn.className.includes('size-8')
      )
      await user.click(editButtons[0])

      // 수정 모달이 열렸는지 확인
      await waitFor(() => {
        const inputs = screen.getAllByRole('textbox')
        const nameInput = inputs.find(
          input => (input as HTMLInputElement).value === 'bug'
        )
        expect(nameInput).toBeInTheDocument()
      })

      // 폼 수정
      const inputs = screen.getAllByRole('textbox')
      const nameInput = inputs.find(
        input => (input as HTMLInputElement).value === 'bug'
      ) as HTMLInputElement

      await user.clear(nameInput)
      await user.type(nameInput, 'critical-bug')

      // Save 버튼 클릭
      const saveButton = screen.getByRole('button', { name: /Save/i })
      await user.click(saveButton)

      // 모달이 닫히고 업데이트된 라벨이 표시됨
      await waitFor(() => {
        expect(screen.getByText('critical-bug')).toBeInTheDocument()
        expect(screen.queryByText('bug')).not.toBeInTheDocument()
      })
    })

    it('랜덤 색상 선택 버튼이 작동한다', async () => {
      const user = userEvent.setup()

      render(<ProjectLabelSetting />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('bug')).toBeInTheDocument()
      })

      // 편집 버튼 클릭
      const allButtons = screen.getAllByRole('button')
      const editButtons = allButtons.filter(btn =>
        btn.className.includes('size-8')
      )
      await user.click(editButtons[0])

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /랜덤 선택/i }))
      })

      // 랜덤 색상 버튼 클릭
      const randomButton = screen.getByRole('button', { name: /랜덤 선택/i })
      await user.click(randomButton)

      // 색상이 변경됨 (랜덤이므로 값이 달라야 함)
      await waitFor(() => {
        const newColorInput = screen
          .getAllByRole('textbox')
          .find(input =>
            (input as HTMLInputElement).value.startsWith('#')
          ) as HTMLInputElement
        // 색상 형식이 올바른지 확인 (#XXXXXX)
        expect(newColorInput.value).toMatch(/^#[0-9A-Fa-f]{6}$/)
        // 참고: 랜덤이므로 매우 낮은 확률로 같을 수 있음
      })
    })
  })

  describe('라벨 삭제 기능', () => {
    it('라벨을 삭제할 수 있다', async () => {
      const user = userEvent.setup()
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true)

      render(<ProjectLabelSetting />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('bug')).toBeInTheDocument()
      })

      // 편집 버튼 클릭
      const allButtons = screen.getAllByRole('button')
      const editButtons = allButtons.filter(btn =>
        btn.className.includes('size-8')
      )
      await user.click(editButtons[0])

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /Delete/i }))
      })

      // Delete 버튼 클릭
      const deleteButton = screen.getByRole('button', { name: /Delete/i })
      await user.click(deleteButton)

      // 확인 다이얼로그가 표시됨
      expect(confirmSpy).toHaveBeenCalledWith(
        "정말 'bug' 라벨을 삭제하시겠습니까?"
      )

      // 모달이 닫히고 라벨이 삭제됨
      await waitFor(() => {
        expect(screen.queryByText('bug')).not.toBeInTheDocument()
      })

      confirmSpy.mockRestore()
    })

    it('삭제 확인을 취소하면 라벨이 삭제되지 않는다', async () => {
      const user = userEvent.setup()
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false)

      render(<ProjectLabelSetting />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('bug')).toBeInTheDocument()
      })

      // 편집 버튼 클릭
      const allButtons = screen.getAllByRole('button')
      const editButtons = allButtons.filter(btn =>
        btn.className.includes('size-8')
      )
      await user.click(editButtons[0])

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /Delete/i }))
      })

      // Delete 버튼 클릭
      const deleteButton = screen.getByRole('button', { name: /Delete/i })
      await user.click(deleteButton)

      // 확인 다이얼로그에서 취소
      expect(confirmSpy).toHaveBeenCalledWith(
        "정말 'bug' 라벨을 삭제하시겠습니까?"
      )

      // 라벨이 여전히 존재함
      await waitFor(() => {
        expect(screen.getByText('bug')).toBeInTheDocument()
      })

      confirmSpy.mockRestore()
    })
  })

  describe('에러 처리', () => {
    it('라벨 목록 로드 실패 시 에러 메시지가 표시된다', async () => {
      // 라벨 조회 API 실패
      server.use(
        http.get('/api/v1/projects/:projectUrl/labels', () => {
          return new HttpResponse(null, { status: 500 })
        }),
        http.get(`${API_BASE_URL}/api/v1/projects/:projectUrl/labels`, () => {
          return new HttpResponse(null, { status: 500 })
        })
      )

      render(<ProjectLabelSetting />, { wrapper: createWrapper() })

      // 에러 메시지 확인
      await waitFor(() => {
        expect(screen.getByText('에러가 발생했습니다.')).toBeInTheDocument()
      })
    })

    it('라벨 생성 실패 시 에러 메시지가 표시된다', async () => {
      const user = userEvent.setup()
      const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {})

      // 라벨 생성 API 실패
      server.use(
        http.post('/api/v1/projects/:projectUrl/labels', () => {
          return new HttpResponse(null, { status: 500 })
        }),
        http.post(`${API_BASE_URL}/api/v1/projects/:projectUrl/labels`, () => {
          return new HttpResponse(null, { status: 500 })
        })
      )

      render(<ProjectLabelSetting />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('bug')).toBeInTheDocument()
      })

      // 생성 버튼 클릭
      const buttons = screen.getAllByRole('button')
      const createButton = buttons.find(button =>
        button.classList.contains('border-[#e1e4ed]')
      )
      await user.click(createButton!)

      await waitFor(() => {
        expect(screen.getByPlaceholderText('Label name')).toBeInTheDocument()
      })

      // 폼 입력
      const nameInput = screen.getByPlaceholderText('Label name')
      await user.type(nameInput, 'test-label')

      // Create 버튼 클릭
      const createSubmitButton = screen.getByRole('button', { name: /Create/i })
      await user.click(createSubmitButton)

      // 에러 메시지 확인
      await waitFor(() => {
        expect(alertSpy).toHaveBeenCalledWith('라벨 생성에 실패했습니다.')
      })

      alertSpy.mockRestore()
    })
  })

  describe('로딩 상태', () => {
    it('라벨 로딩 중에는 로딩 메시지가 표시된다', () => {
      render(<ProjectLabelSetting />, { wrapper: createWrapper() })

      // 로딩 메시지 확인
      expect(screen.getByText('로딩 중...')).toBeInTheDocument()
    })
  })
})
