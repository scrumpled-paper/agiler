import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import UserProfileBox from './UserProfileBox'

// Mock UI components
vi.mock('./ui/button', () => ({
  Button: ({
    children,
    onClick,
    disabled,
  }: {
    children: React.ReactNode
    onClick: () => void
    disabled?: boolean
  }) => (
    <button onClick={onClick} disabled={disabled}>
      {children}
    </button>
  ),
}))

// Mock hooks
vi.mock('@/hooks/use-user', () => ({
  useUserInfo: vi.fn(() => ({
    nickname: 'userName',
    email: 'user@example.com',
    imageUrl: null,
    description: '',
  })),
  useDashboardProfileMutation: vi.fn(() => ({
    mutate: vi.fn(),
    isPending: false,
    isSuccess: false,
    isError: false,
  })),
  useProjectProfileMutation: vi.fn(() => ({
    mutate: vi.fn(),
    isPending: false,
    isSuccess: false,
    isError: false,
  })),
  useDashboardImageUploadMutation: vi.fn(() => ({
    mutate: vi.fn(),
    isPending: false,
    isSuccess: false,
    isError: false,
  })),
  useProjectImageUploadMutation: vi.fn(() => ({
    mutate: vi.fn(),
    isPending: false,
    isSuccess: false,
    isError: false,
  })),
  useDashboardImageDeleteMutation: vi.fn(() => ({
    mutate: vi.fn(),
    isPending: false,
    isSuccess: false,
    isError: false,
  })),
  useProjectImageDeleteMutation: vi.fn(() => ({
    mutate: vi.fn(),
    isPending: false,
    isSuccess: false,
    isError: false,
  })),
}))

const createWrapper = () => {
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
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    )
  }
}

describe('UserProfileBox', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render user profile', () => {
    render(<UserProfileBox context="dashboard" />, { wrapper: createWrapper() })
    expect(screen.getByText('Name')).toBeInTheDocument()
    expect(screen.getByText('Email')).toBeInTheDocument()
  })

  it('should render default username', () => {
    render(<UserProfileBox context="dashboard" />, { wrapper: createWrapper() })
    expect(screen.getByDisplayValue('userName')).toBeInTheDocument()
  })

  it('should enable edit mode when edit button is clicked', () => {
    render(<UserProfileBox context="dashboard" />, { wrapper: createWrapper() })
    const input = screen.getByDisplayValue('userName') as HTMLInputElement

    expect(input.disabled).toBe(true)

    // Click edit button
    const editButton = screen.getByText('Edit')
    fireEvent.click(editButton)

    expect(input.disabled).toBe(false)
  })

  it('should update username when typing in edit mode', () => {
    render(<UserProfileBox context="dashboard" />, { wrapper: createWrapper() })

    // Enable edit mode
    const editButton = screen.getByText('Edit')
    fireEvent.click(editButton)

    const input = screen.getByDisplayValue('userName')
    fireEvent.change(input, { target: { value: 'newUserName' } })

    expect(screen.getByDisplayValue('newUserName')).toBeInTheDocument()
  })

  it('should render project description field when context is project', () => {
    render(<UserProfileBox context="project" projectUrl="test-project" />, {
      wrapper: createWrapper(),
    })

    expect(screen.getByText('Description')).toBeInTheDocument()
  })

  it('should not render project description field when context is dashboard', () => {
    render(<UserProfileBox context="dashboard" />, { wrapper: createWrapper() })

    expect(screen.queryByText('Description')).not.toBeInTheDocument()
  })

  it('should display Cancel button when in edit mode', () => {
    render(<UserProfileBox context="dashboard" />, { wrapper: createWrapper() })

    // Initially no Cancel button
    expect(screen.queryByText('Cancel')).not.toBeInTheDocument()

    // Enable edit mode
    const editButton = screen.getByText('Edit')
    fireEvent.click(editButton)

    // Cancel button should appear
    expect(screen.getByText('Cancel')).toBeInTheDocument()
  })

  it('should exit edit mode when Cancel button is clicked', () => {
    render(<UserProfileBox context="dashboard" />, { wrapper: createWrapper() })

    // Enable edit mode
    const editButton = screen.getByText('Edit')
    fireEvent.click(editButton)

    const input = screen.getByDisplayValue('userName') as HTMLInputElement
    expect(input.disabled).toBe(false)

    // Click Cancel
    const cancelButton = screen.getByText('Cancel')
    fireEvent.click(cancelButton)

    // Should exit edit mode
    expect(input.disabled).toBe(true)
    expect(screen.queryByText('Cancel')).not.toBeInTheDocument()
  })
})
