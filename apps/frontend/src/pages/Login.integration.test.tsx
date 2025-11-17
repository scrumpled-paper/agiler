import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import userEvent from '@testing-library/user-event'
import Login from './Login'

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'https://agiler.p-e.kr'

describe('Login Page - 통합 테스트', () => {
  beforeEach(() => {
    // Vitest의 네이티브 global mocking 사용
    vi.stubGlobal('location', { href: '' })
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  describe('UI 렌더링', () => {
    it('로그인 페이지가 올바르게 렌더링된다', () => {
      render(
        <MemoryRouter>
          <Login />
        </MemoryRouter>
      )

      // 타이틀 확인
      expect(screen.getByText('Welcome to Agiler')).toBeInTheDocument()

      // 소셜 로그인 버튼 확인
      expect(
        screen.getByRole('button', { name: /continue with google/i })
      ).toBeInTheDocument()
      expect(
        screen.getByRole('button', { name: /continue with github/i })
      ).toBeInTheDocument()
    })

    it('에러 파라미터가 있을 때 에러 메시지가 표시된다', () => {
      // MemoryRouter의 initialEntries를 사용하여 쿼리 파라미터 설정
      render(
        <MemoryRouter initialEntries={['/login?error=invalid_credentials']}>
          <Login />
        </MemoryRouter>
      )

      expect(screen.getByText(/login failed/i)).toBeInTheDocument()
      expect(screen.getByText(/invalid_credentials/i)).toBeInTheDocument()
    })

    it('에러 파라미터가 없을 때 에러 메시지가 표시되지 않는다', () => {
      render(
        <MemoryRouter>
          <Login />
        </MemoryRouter>
      )

      expect(screen.queryByText(/login failed/i)).not.toBeInTheDocument()
    })
  })

  describe('OAuth 소셜 로그인', () => {
    it('Google 로그인 버튼을 클릭하면 Google OAuth URL로 리다이렉트된다', async () => {
      const user = userEvent.setup()

      render(
        <MemoryRouter>
          <Login />
        </MemoryRouter>
      )

      const googleButton = screen.getByRole('button', {
        name: /continue with google/i,
      })
      await user.click(googleButton)

      // window.location.href가 올바른 OAuth URL로 설정되었는지 확인
      expect(window.location.href).toBe(
        `${API_BASE_URL}/api/oauth2/authorization/google`
      )
    })

    it('GitHub 로그인 버튼을 클릭하면 GitHub OAuth URL로 리다이렉트된다', async () => {
      const user = userEvent.setup()

      render(
        <MemoryRouter>
          <Login />
        </MemoryRouter>
      )

      const githubButton = screen.getByRole('button', {
        name: /continue with github/i,
      })
      await user.click(githubButton)

      // window.location.href가 올바른 OAuth URL로 설정되었는지 확인
      expect(window.location.href).toBe(
        `${API_BASE_URL}/api/oauth2/authorization/github`
      )
    })
  })

  describe('접근성', () => {
    it('모든 버튼이 키보드로 접근 가능하다', async () => {
      const user = userEvent.setup()

      render(
        <MemoryRouter>
          <Login />
        </MemoryRouter>
      )

      // Tab으로 Google 버튼에 포커스
      await user.tab()
      const googleButton = screen.getByRole('button', {
        name: /continue with google/i,
      })
      expect(googleButton).toHaveFocus()

      // Tab으로 GitHub 버튼에 포커스
      await user.tab()
      const githubButton = screen.getByRole('button', {
        name: /continue with github/i,
      })
      expect(githubButton).toHaveFocus()
    })
  })
})
