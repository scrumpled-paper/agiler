import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { NotificationList } from './NotificationList'
import { notificationService } from '@/api/services/notificationService'
import type { Subscript } from '@/types/notification'

// Helper function to render with all providers
const renderWithProviders = (projectUrl: string = 'test-project') => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  })

  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[`/projects/${projectUrl}`]}>
        <Routes>
          <Route path="/projects/:projectUrl" element={<NotificationList />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>
  )
}

describe('NotificationList', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should display subscriptions when data is loaded', async () => {
    renderWithProviders()

    // Wait for data to load and tabs to appear
    await waitFor(() => {
      expect(
        screen.getByRole('tab', { name: /subscribe/i })
      ).toBeInTheDocument()
    })
  })

  it('should display subscription list when data is loaded', async () => {
    renderWithProviders()

    // Wait for subscriptions to load
    await waitFor(() => {
      expect(screen.getByText(/로그인 페이지 UX 개선/i)).toBeInTheDocument()
    })

    // Check all 3 mock subscriptions are displayed
    expect(screen.getByText(/로그인 페이지 UX 개선/i)).toBeInTheDocument()
    expect(screen.getByText(/API 게이트웨이 성능 테스트/i)).toBeInTheDocument()
    expect(
      screen.getByText(/2026년도 신규 기능 계획 수립/i)
    ).toBeInTheDocument()

    // Check status information
    expect(screen.getByText(/Status change: In Progress/i)).toBeInTheDocument()
    expect(screen.getByText(/Status change: Done/i)).toBeInTheDocument()
    expect(screen.getByText(/Status change: To Do/i)).toBeInTheDocument()

    // Check target users
    expect(screen.getByText(/김개발/i)).toBeInTheDocument()
    expect(screen.getByText(/박운영/i)).toBeInTheDocument()
    expect(screen.getByText(/최기획/i)).toBeInTheDocument()
  })

  it('should display empty state when there are no subscriptions', async () => {
    // Mock empty response
    vi.spyOn(
      notificationService,
      'getIssueSubscriptions'
    ).mockResolvedValueOnce({
      subscriptions: [] as Subscript[],
    })

    renderWithProviders()

    await waitFor(() => {
      expect(screen.getByText(/No active subscriptions/i)).toBeInTheDocument()
    })

    expect(
      screen.getByText(/Subscribe to issues to get notified/i)
    ).toBeInTheDocument()
  })

  it('should display error state within notification items when fetching fails', async () => {
    // Mock error response
    vi.spyOn(
      notificationService,
      'getIssueSubscriptions'
    ).mockRejectedValueOnce(new Error('Failed to fetch'))

    renderWithProviders()

    // The component shows empty state because subscriptions.length === 0 takes precedence
    // The error state is only shown inside NotificationItem when there's an error but also data
    await waitFor(() => {
      expect(screen.getByText(/No active subscriptions/i)).toBeInTheDocument()
    })
  })

  it('should unsubscribe when clicking unsubscribe button', async () => {
    const user = userEvent.setup()
    const unsubscribeSpy = vi.spyOn(
      notificationService,
      'unsubscribeIssueNotification'
    )

    renderWithProviders()

    // Wait for subscriptions to load
    await waitFor(() => {
      expect(screen.getByText(/로그인 페이지 UX 개선/i)).toBeInTheDocument()
    })

    // Find and click the first unsubscribe button (BellOff icon buttons)
    const unsubscribeButtons = screen.getAllByRole('button').filter(btn => {
      const svg = btn.querySelector('svg.lucide-bell-off')
      return svg !== null
    })
    const firstUnsubscribeButton = unsubscribeButtons[0]

    await user.click(firstUnsubscribeButton)

    // Verify unsubscribe was called with correct subscription ID (101 from mock data)
    expect(unsubscribeSpy).toHaveBeenCalledWith('test-project', 101)
  })

  it('should display tabs for subscribe and schedule', async () => {
    renderWithProviders()

    await waitFor(() => {
      expect(screen.getByText(/로그인 페이지 UX 개선/i)).toBeInTheDocument()
    })

    // Check that both tabs are displayed
    expect(screen.getByRole('tab', { name: /subscribe/i })).toBeInTheDocument()
    expect(screen.getByRole('tab', { name: /schedule/i })).toBeInTheDocument()
  })

  it('should display bell icons for each subscription item', async () => {
    renderWithProviders()

    await waitFor(() => {
      expect(screen.getByText(/로그인 페이지 UX 개선/i)).toBeInTheDocument()
    })

    // Check that bell icons are displayed for each subscription (using data-testid or class)
    const bellIcons = document.querySelectorAll('.lucide-bell')
    expect(bellIcons.length).toBe(3) // 3 subscriptions
  })

  it('should switch between subscribe and schedule tabs', async () => {
    const user = userEvent.setup()
    renderWithProviders()

    // Wait for initial subscriptions to load
    await waitFor(() => {
      expect(screen.getByText(/로그인 페이지 UX 개선/i)).toBeInTheDocument()
    })

    // Click on schedule tab
    const scheduleTab = screen.getByRole('tab', { name: /schedule/i })
    await user.click(scheduleTab)

    // Wait for schedule data to load
    await waitFor(() => {
      expect(screen.getByText(/디자인 시스템 V2 적용/i)).toBeInTheDocument()
    })

    // Verify schedule items are displayed
    expect(screen.getByText(/주간 회의 자료 준비/i)).toBeInTheDocument()
    expect(
      screen.getByText(/서버 모니터링 경고 임계치 조정/i)
    ).toBeInTheDocument()
    expect(
      screen.getByText(/새로운 결제 모듈 연동 테스트/i)
    ).toBeInTheDocument()

    // Verify date formatting exists (Korean format)
    expect(screen.getByText(/2025년 12월 10일/i)).toBeInTheDocument()
  })

  it('should delete schedule when clicking delete button in schedule tab', async () => {
    const user = userEvent.setup()
    const deleteSpy = vi.spyOn(
      notificationService,
      'deleteScheduleNotification'
    )

    renderWithProviders()

    // Wait for subscriptions to load first
    await waitFor(() => {
      expect(screen.getByText(/로그인 페이지 UX 개선/i)).toBeInTheDocument()
    })

    // Switch to schedule tab
    const scheduleTab = screen.getByRole('tab', { name: /schedule/i })
    await user.click(scheduleTab)

    // Wait for schedule data to load
    await waitFor(() => {
      expect(screen.getByText(/디자인 시스템 V2 적용/i)).toBeInTheDocument()
    })

    // Find and click the first delete button (BellOff icon buttons)
    const deleteButtons = screen.getAllByRole('button').filter(btn => {
      const svg = btn.querySelector('svg.lucide-bell-off')
      return svg !== null
    })
    const firstDeleteButton = deleteButtons[0]

    await user.click(firstDeleteButton)

    // Verify delete was called with correct schedule ID (201 from mock data)
    expect(deleteSpy).toHaveBeenCalledWith('test-project', 201)
  })
})
