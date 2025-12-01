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
  })),
  useProjectProfileMutation: vi.fn(() => ({
    mutate: vi.fn(),
  })),
}))

// Mock s3Service
vi.mock('@/api/services/s3Service', () => ({
  s3Service: {
    uploadProfileImage: vi.fn(),
  },
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
})
