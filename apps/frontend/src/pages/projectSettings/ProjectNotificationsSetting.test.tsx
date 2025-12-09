import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import ProjectNotificationsSetting from './ProjectNotificationsSetting'
import { notificationService } from '@/api/services/notificationService'
import { resetNotificationChannelsStore } from '@/mocks/handlers'

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
      <MemoryRouter
        initialEntries={[`/projects/${projectUrl}/settings/notifications`]}
      >
        <Routes>
          <Route
            path="/projects/:projectUrl/settings/notifications"
            element={<ProjectNotificationsSetting />}
          />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>
  )
}

describe('ProjectNotificationsSetting', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    resetNotificationChannelsStore()
  })

  it('should render page title and description', async () => {
    renderWithProviders()

    expect(
      screen.getByRole('heading', { name: /Notification Channels/i })
    ).toBeInTheDocument()
    expect(
      screen.getByText(/Slack 또는 Discord 채널을 연동하세요/i)
    ).toBeInTheDocument()
  })

  it('should display error when projectUrl is missing', () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    })

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/settings/notifications']}>
          <Routes>
            <Route
              path="/settings/notifications"
              element={<ProjectNotificationsSetting />}
            />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    )

    expect(
      screen.getByText(/프로젝트 URL을 찾을 수 없습니다/i)
    ).toBeInTheDocument()
  })

  it('should display loading state initially', () => {
    renderWithProviders()

    expect(screen.getAllByText(/로딩 중.../i)).toHaveLength(2)
  })

  it('should display Slack and Discord sections with channel counts', async () => {
    renderWithProviders()

    // Wait for data to load
    await waitFor(() => {
      expect(screen.queryByText(/로딩 중.../i)).not.toBeInTheDocument()
    })

    // Check Slack and Discord headings exist
    expect(screen.getByRole('heading', { name: /Slack/i })).toBeInTheDocument()
    expect(
      screen.getByRole('heading', { name: /Discord/i })
    ).toBeInTheDocument()

    // Check for channel count badges (they should show "1개 연동됨")
    const badges = screen.getAllByText(/1개 연동됨/i)
    expect(badges).toHaveLength(2) // One for Slack, one for Discord
  })

  it('should display connected Slack channels', async () => {
    renderWithProviders()

    await waitFor(() => {
      expect(screen.queryByText(/로딩 중.../i)).not.toBeInTheDocument()
    })

    // Find Slack section and check for channel
    const slackChannels = screen.getAllByText(/slack/i)
    expect(slackChannels.length).toBeGreaterThan(0)

    // Check for "활성" badge
    const activeBadges = screen.getAllByText(/활성/i)
    expect(activeBadges.length).toBeGreaterThan(0)
  })

  it('should display connected Discord channels', async () => {
    renderWithProviders()

    await waitFor(() => {
      expect(screen.queryByText(/로딩 중.../i)).not.toBeInTheDocument()
    })

    // Find Discord section and check for channel
    const discordChannels = screen.getAllByText(/discord/i)
    expect(discordChannels.length).toBeGreaterThan(0)
  })

  it('should show empty state when no channels are connected', async () => {
    // Mock empty response
    vi.spyOn(
      notificationService,
      'getRegisteredChannels'
    ).mockResolvedValueOnce({
      channels: [],
    })

    renderWithProviders()

    await waitFor(() => {
      expect(
        screen.getByText(/연동된 Slack 채널이 없습니다/i)
      ).toBeInTheDocument()
    })

    expect(
      screen.getByText(/연동된 Discord 채널이 없습니다/i)
    ).toBeInTheDocument()
  })

  it('should delete channel when clicking delete button', async () => {
    const user = userEvent.setup()
    const deleteChannelSpy = vi.spyOn(notificationService, 'deleteChannel')

    renderWithProviders()

    await waitFor(() => {
      expect(screen.queryByText(/로딩 중.../i)).not.toBeInTheDocument()
    })

    // Find and click the first delete button
    const deleteButtons = screen.getAllByRole('button', { name: '' })
    const firstDeleteButton = deleteButtons.find(button =>
      button.querySelector('.text-destructive')
    )

    if (firstDeleteButton) {
      await user.click(firstDeleteButton)

      // Verify delete was called with correct channel ID
      expect(deleteChannelSpy).toHaveBeenCalledWith('test-project', 1)
    }
  })

  it('should have Slack and Discord integration buttons', async () => {
    renderWithProviders()

    await waitFor(() => {
      expect(screen.queryByText(/로딩 중.../i)).not.toBeInTheDocument()
    })

    // Check that integration buttons exist
    const slackButton = screen.getByText(/Slack 연동하기/i)
    const discordButton = screen.getByText(/Discord 연동하기/i)

    expect(slackButton).toBeInTheDocument()
    expect(discordButton).toBeInTheDocument()
  })

  it('should display error state when fetching channels fails', async () => {
    // Mock error response
    vi.spyOn(
      notificationService,
      'getRegisteredChannels'
    ).mockRejectedValueOnce(new Error('Failed to fetch channels'))

    renderWithProviders()

    await waitFor(() => {
      expect(
        screen.getByText(/알림 채널 정보를 불러오는데 실패했습니다/i)
      ).toBeInTheDocument()
    })
  })

  it('should show correct channel counts for mixed channels', async () => {
    // Mock response with multiple channels of each type
    vi.spyOn(
      notificationService,
      'getRegisteredChannels'
    ).mockResolvedValueOnce({
      channels: [
        { id: 1, channelType: 'slack' },
        { id: 2, channelType: 'slack' },
        { id: 3, channelType: 'discord' },
      ],
    })

    renderWithProviders()

    await waitFor(() => {
      expect(screen.queryByText(/로딩 중.../i)).not.toBeInTheDocument()
    })

    // Check counts using getAllByText
    expect(screen.getByText(/2개 연동됨/i)).toBeInTheDocument()
    expect(screen.getByText(/1개 연동됨/i)).toBeInTheDocument()
  })
})
