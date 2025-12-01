import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import userEvent from '@testing-library/user-event'
import ProjectTemplateSetting from './ProjectTemplateSetting'
import { server } from '@/mocks/server'
import { resetTemplatesStore } from '@/mocks/handlers'
import { http, HttpResponse } from 'msw'

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'https://agiler.p-e.kr'

const createWrapper = (
  initialRoute = '/projects/test-project/settings/project-template'
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
              path="/projects/:projectUrl/settings/project-template"
              element={children}
            />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    )
  }
}

describe('ProjectTemplateSetting Page - 통합 테스트', () => {
  beforeEach(() => {
    server.resetHandlers()
    resetTemplatesStore()
    vi.clearAllMocks()
  })

  describe('UI 렌더링', () => {
    it('템플릿 설정 페이지가 올바르게 렌더링된다', async () => {
      render(<ProjectTemplateSetting />, { wrapper: createWrapper() })

      // 템플릿 목록 로드 대기
      await waitFor(() => {
        expect(screen.getByText('Bug Report Template')).toBeInTheDocument()
      })

      // 페이지 타이틀 확인
      expect(screen.getByText('Template Setting')).toBeInTheDocument()
    })

    it('카테고리 필터가 표시된다', async () => {
      render(<ProjectTemplateSetting />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('Bug Report Template')).toBeInTheDocument()
      })

      // 카테고리 레이블 확인
      expect(screen.getByText('Category')).toBeInTheDocument()

      // Select 컴포넌트 확인
      const selectTrigger = screen.getByRole('combobox')
      expect(selectTrigger).toBeInTheDocument()
    })

    it('Issue 템플릿 목록이 올바르게 표시된다', async () => {
      render(<ProjectTemplateSetting />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('Bug Report Template')).toBeInTheDocument()
        expect(screen.getByText('Feature Request Template')).toBeInTheDocument()
      })

      // 템플릿 설명 확인
      expect(
        screen.getByText('Template for reporting bugs')
      ).toBeInTheDocument()
      expect(
        screen.getByText('Template for requesting new features')
      ).toBeInTheDocument()
    })

    it('새 템플릿 생성 버튼이 표시된다', async () => {
      render(<ProjectTemplateSetting />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('Bug Report Template')).toBeInTheDocument()
      })

      // Plus 아이콘 버튼이 있는지 확인
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

  describe('카테고리 필터 기능', () => {
    // Skip: jsdom에서 Radix UI Select의 scrollIntoView 미지원
    it.skip('카테고리를 변경하면 해당 카테고리의 템플릿이 표시된다', async () => {
      const user = userEvent.setup()

      render(<ProjectTemplateSetting />, { wrapper: createWrapper() })

      // Issue 템플릿 확인
      await waitFor(() => {
        expect(screen.getByText('Bug Report Template')).toBeInTheDocument()
      })

      // Select 열기
      const selectTrigger = screen.getByRole('combobox')
      await user.click(selectTrigger)

      // Meetings 옵션 선택
      await waitFor(() => {
        expect(screen.getByRole('option', { name: 'Meetings' }))
      })
      const meetingsOption = screen.getByRole('option', { name: 'Meetings' })
      await user.click(meetingsOption)

      // Meeting 템플릿이 표시됨
      await waitFor(() => {
        expect(screen.getByText('Sprint Planning')).toBeInTheDocument()
        expect(
          screen.queryByText('Bug Report Template')
        ).not.toBeInTheDocument()
      })
    })

    // Skip: jsdom에서 Radix UI Select의 scrollIntoView 미지원
    it.skip('Retros 카테고리로 전환할 수 있다', async () => {
      const user = userEvent.setup()

      render(<ProjectTemplateSetting />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('Bug Report Template')).toBeInTheDocument()
      })

      // Select 열기
      const selectTrigger = screen.getByRole('combobox')
      await user.click(selectTrigger)

      // Retros 옵션 선택
      await waitFor(() => {
        expect(screen.getByRole('option', { name: 'Retros' }))
      })
      const retrosOption = screen.getByRole('option', { name: 'Retros' })
      await user.click(retrosOption)

      // Retro 템플릿이 표시됨
      await waitFor(() => {
        expect(screen.getByText('Sprint Retrospective')).toBeInTheDocument()
      })
    })

    // Skip: jsdom에서 Radix UI Select의 scrollIntoView 미지원
    it.skip('Scrums 카테고리로 전환할 수 있다', async () => {
      const user = userEvent.setup()

      render(<ProjectTemplateSetting />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('Bug Report Template')).toBeInTheDocument()
      })

      // Select 열기
      const selectTrigger = screen.getByRole('combobox')
      await user.click(selectTrigger)

      // Scrums 옵션 선택
      await waitFor(() => {
        expect(screen.getByRole('option', { name: 'Scrums' }))
      })
      const scrumsOption = screen.getByRole('option', { name: 'Scrums' })
      await user.click(scrumsOption)

      // Scrum 템플릿이 표시됨
      await waitFor(() => {
        expect(screen.getByText('Daily Standup')).toBeInTheDocument()
      })
    })
  })

  describe('템플릿 생성 기능', () => {
    it('새 템플릿을 생성할 수 있다', async () => {
      const user = userEvent.setup()

      render(<ProjectTemplateSetting />, { wrapper: createWrapper() })

      // 템플릿 목록 로드 대기
      await waitFor(() => {
        expect(screen.getByText('Bug Report Template')).toBeInTheDocument()
      })

      // 생성 버튼 클릭
      const buttons = screen.getAllByRole('button')
      const createButton = buttons.find(button =>
        button.classList.contains('border-[#e1e4ed]')
      )
      await user.click(createButton!)

      // 모달이 열렸는지 확인
      await waitFor(() => {
        expect(
          screen.getByPlaceholderText('Template title')
        ).toBeInTheDocument()
      })

      // 폼 입력
      const titleInput = screen.getByPlaceholderText('Template title')
      const descriptionInput = screen.getByPlaceholderText(
        'Template description'
      )

      await user.type(titleInput, 'New Test Template')
      await user.type(descriptionInput, 'Description for test template')

      // 마크다운 에디터 찾기 (textarea)
      const textareas = screen.getAllByRole('textbox')
      const markdownEditor = textareas.find(
        textarea =>
          textarea.className.includes('w-md-editor-text-input') ||
          textarea.getAttribute('data-color-mode')
      )
      if (markdownEditor) {
        await user.type(markdownEditor, '# Test Content\n\nThis is a test.')
      }

      // Create 버튼 클릭
      const createSubmitButton = screen.getByRole('button', { name: /Create/i })
      await user.click(createSubmitButton)

      // 모달이 닫히고 새 템플릿이 목록에 추가됨
      await waitFor(() => {
        expect(
          screen.queryByPlaceholderText('Template title')
        ).not.toBeInTheDocument()
        expect(screen.getByText('New Test Template')).toBeInTheDocument()
      })
    })

    it('템플릿 제목 없이 생성하면 경고가 표시된다', async () => {
      const user = userEvent.setup()
      const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {})

      render(<ProjectTemplateSetting />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('Bug Report Template')).toBeInTheDocument()
      })

      // 생성 버튼 클릭
      const buttons = screen.getAllByRole('button')
      const createButton = buttons.find(button =>
        button.classList.contains('border-[#e1e4ed]')
      )
      await user.click(createButton!)

      await waitFor(() => {
        expect(
          screen.getByPlaceholderText('Template title')
        ).toBeInTheDocument()
      })

      // 제목 없이 Create 버튼 클릭
      const createSubmitButton = screen.getByRole('button', { name: /Create/i })
      await user.click(createSubmitButton)

      // 경고 메시지 확인
      expect(alertSpy).toHaveBeenCalledWith('템플릿 제목을 입력해주세요.')

      alertSpy.mockRestore()
    })

    it('Cancel 버튼을 클릭하면 모달이 닫힌다', async () => {
      const user = userEvent.setup()

      render(<ProjectTemplateSetting />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('Bug Report Template')).toBeInTheDocument()
      })

      // 생성 버튼 클릭
      const buttons = screen.getAllByRole('button')
      const createButton = buttons.find(button =>
        button.classList.contains('border-[#e1e4ed]')
      )
      await user.click(createButton!)

      await waitFor(() => {
        expect(
          screen.getByPlaceholderText('Template title')
        ).toBeInTheDocument()
      })

      // Cancel 버튼 클릭
      const cancelButton = screen.getByRole('button', { name: /Cancel/i })
      await user.click(cancelButton)

      // 모달이 닫힘
      await waitFor(() => {
        expect(
          screen.queryByPlaceholderText('Template title')
        ).not.toBeInTheDocument()
      })
    })
  })

  describe('템플릿 수정 기능', () => {
    it('템플릿을 수정할 수 있다', async () => {
      const user = userEvent.setup()

      render(<ProjectTemplateSetting />, { wrapper: createWrapper() })

      // 템플릿 목록 로드 대기
      await waitFor(() => {
        expect(screen.getByText('Bug Report Template')).toBeInTheDocument()
      })

      // 첫 번째 템플릿의 편집 버튼 클릭 (MoreHorizontal icon)
      const allButtons = screen.getAllByRole('button')
      const editButtons = allButtons.filter(btn =>
        btn.className.includes('size-8')
      )
      await user.click(editButtons[0])

      // 수정 모달이 열렸는지 확인
      await waitFor(
        () => {
          const inputs = screen.getAllByRole('textbox')
          const titleInput = inputs.find(
            input => (input as HTMLInputElement).value === 'Bug Report Template'
          )
          expect(titleInput).toBeInTheDocument()
        },
        { timeout: 3000 }
      )

      // 폼 수정
      const inputs = screen.getAllByRole('textbox')
      const titleInput = inputs.find(
        input => (input as HTMLInputElement).value === 'Bug Report Template'
      ) as HTMLInputElement

      await user.clear(titleInput)
      await user.type(titleInput, 'Updated Bug Report')

      // Save 버튼 클릭
      const saveButton = screen.getByRole('button', { name: /Save/i })
      await user.click(saveButton)

      // 모달이 닫히고 업데이트된 템플릿이 표시됨
      await waitFor(() => {
        expect(screen.getByText('Updated Bug Report')).toBeInTheDocument()
        expect(
          screen.queryByText('Bug Report Template')
        ).not.toBeInTheDocument()
      })
    })
  })

  describe('템플릿 삭제 기능', () => {
    it('템플릿을 삭제할 수 있다', async () => {
      const user = userEvent.setup()
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true)

      render(<ProjectTemplateSetting />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('Bug Report Template')).toBeInTheDocument()
      })

      // 편집 버튼 클릭
      const allButtons = screen.getAllByRole('button')
      const editButtons = allButtons.filter(btn =>
        btn.className.includes('size-8')
      )
      await user.click(editButtons[0])

      await waitFor(
        () => {
          expect(screen.getByRole('button', { name: /Delete/i }))
        },
        { timeout: 3000 }
      )

      // Delete 버튼 클릭
      const deleteButton = screen.getByRole('button', { name: /Delete/i })
      await user.click(deleteButton)

      // 확인 다이얼로그가 표시됨
      expect(confirmSpy).toHaveBeenCalledWith(
        "정말 'Bug Report Template' 템플릿을 삭제하시겠습니까?"
      )

      // 모달이 닫히고 템플릿이 삭제됨
      await waitFor(() => {
        expect(
          screen.queryByText('Bug Report Template')
        ).not.toBeInTheDocument()
      })

      confirmSpy.mockRestore()
    })

    it('삭제 확인을 취소하면 템플릿이 삭제되지 않는다', async () => {
      const user = userEvent.setup()
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false)

      render(<ProjectTemplateSetting />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('Bug Report Template')).toBeInTheDocument()
      })

      // 편집 버튼 클릭
      const allButtons = screen.getAllByRole('button')
      const editButtons = allButtons.filter(btn =>
        btn.className.includes('size-8')
      )
      await user.click(editButtons[0])

      await waitFor(
        () => {
          expect(screen.getByRole('button', { name: /Delete/i }))
        },
        { timeout: 3000 }
      )

      // Delete 버튼 클릭
      const deleteButton = screen.getByRole('button', { name: /Delete/i })
      await user.click(deleteButton)

      // 확인 다이얼로그에서 취소
      expect(confirmSpy).toHaveBeenCalledWith(
        "정말 'Bug Report Template' 템플릿을 삭제하시겠습니까?"
      )

      // 템플릿이 여전히 존재함
      await waitFor(() => {
        expect(screen.getByText('Bug Report Template')).toBeInTheDocument()
      })

      confirmSpy.mockRestore()
    })
  })

  describe('에러 처리', () => {
    it('템플릿 목록 로드 실패 시 에러 메시지가 표시된다', async () => {
      // 템플릿 조회 API 실패
      server.use(
        http.get('/api/v1/projects/:projectUrl/issues/templates', () => {
          return new HttpResponse(null, { status: 500 })
        }),
        http.get(
          `${API_BASE_URL}/api/v1/projects/:projectUrl/issues/templates`,
          () => {
            return new HttpResponse(null, { status: 500 })
          }
        )
      )

      render(<ProjectTemplateSetting />, { wrapper: createWrapper() })

      // 에러 메시지 확인
      await waitFor(() => {
        expect(screen.getByText('에러가 발생했습니다.')).toBeInTheDocument()
      })
    })

    it('템플릿 생성 실패 시 에러 메시지가 표시된다', async () => {
      const user = userEvent.setup()
      const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {})

      // 템플릿 생성 API 실패
      server.use(
        http.post('/api/v1/projects/:projectUrl/issues/templates', () => {
          return new HttpResponse(null, { status: 500 })
        }),
        http.post(
          `${API_BASE_URL}/api/v1/projects/:projectUrl/issues/templates`,
          () => {
            return new HttpResponse(null, { status: 500 })
          }
        )
      )

      render(<ProjectTemplateSetting />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('Bug Report Template')).toBeInTheDocument()
      })

      // 생성 버튼 클릭
      const buttons = screen.getAllByRole('button')
      const createButton = buttons.find(button =>
        button.classList.contains('border-[#e1e4ed]')
      )
      await user.click(createButton!)

      await waitFor(() => {
        expect(
          screen.getByPlaceholderText('Template title')
        ).toBeInTheDocument()
      })

      // 폼 입력
      const titleInput = screen.getByPlaceholderText('Template title')
      await user.type(titleInput, 'test-template')

      // Create 버튼 클릭
      const createSubmitButton = screen.getByRole('button', { name: /Create/i })
      await user.click(createSubmitButton)

      // 에러 메시지 확인
      await waitFor(() => {
        expect(alertSpy).toHaveBeenCalledWith('템플릿 생성에 실패했습니다.')
      })

      alertSpy.mockRestore()
    })
  })

  describe('로딩 상태', () => {
    it('템플릿 로딩 중에는 로딩 메시지가 표시된다', () => {
      render(<ProjectTemplateSetting />, { wrapper: createWrapper() })

      // 로딩 메시지 확인
      expect(screen.getByText('로딩 중...')).toBeInTheDocument()
    })
  })
})
