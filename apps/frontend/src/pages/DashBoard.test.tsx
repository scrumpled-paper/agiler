import { describe, it, expect, vi, beforeEach } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithRouter } from '@/test-utils/render-with-router'
import DashBoard from './DashBoard'

// Mock components
vi.mock('@/components/UserProfileBox', () => ({
  default: () => <div data-testid="user-profile-box">User Profile Box</div>,
}))

vi.mock('@/components/ProjectList', () => ({
  default: ({
    contents,
    currentPage,
    totalPages,
    onPageChange,
  }: {
    contents: unknown[]
    currentPage: number
    totalPages: number
    onPageChange: (page: number) => void
  }) => (
    <div data-testid="project-list">
      <div data-testid="contents-count">{contents.length}</div>
      <div data-testid="current-page">{currentPage}</div>
      <div data-testid="total-pages">{totalPages}</div>
      <button onClick={() => onPageChange(currentPage + 1)}>Next Page</button>
    </div>
  ),
}))

// Mock utils
const mockFetchMockContents = vi.fn()
vi.mock('@/utils/mockData', () => ({
  fetchMockContents: (...args: unknown[]) => mockFetchMockContents(...args),
}))

describe('DashBoard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render loading state initially', () => {
    mockFetchMockContents.mockReturnValue(
      new Promise(() => {
        /* never resolves */
      })
    )

    renderWithRouter(<DashBoard />, { initialEntries: ['/dashboard'] })

    expect(screen.getByText('로딩 중...')).toBeInTheDocument()
  })

  it('should render dashboard with data', async () => {
    const mockData = {
      contents: [
        {
          title: 'Project 1',
          url: '/projects/1',
          imageUrl: 'https://placehold.co/600x400',
          summary: 'Test project 1',
        },
        {
          title: 'Project 2',
          url: '/projects/2',
          imageUrl: 'https://placehold.co/600x400',
          summary: 'Test project 2',
        },
      ],
      totalPages: 5,
    }

    mockFetchMockContents.mockResolvedValue(mockData)

    renderWithRouter(<DashBoard />, { initialEntries: ['/dashboard'] })

    await waitFor(() => {
      expect(screen.getByTestId('user-profile-box')).toBeInTheDocument()
    })

    expect(screen.getByText('Todo List')).toBeInTheDocument()
    expect(screen.getByTestId('project-list')).toBeInTheDocument()
    expect(screen.getByTestId('contents-count')).toHaveTextContent('2')
    expect(screen.getByTestId('current-page')).toHaveTextContent('1')
    expect(screen.getByTestId('total-pages')).toHaveTextContent('5')
  })

  it('should render error state when query fails', async () => {
    mockFetchMockContents.mockRejectedValue(new Error('Failed to fetch'))

    renderWithRouter(<DashBoard />, { initialEntries: ['/dashboard'] })

    await waitFor(() => {
      expect(screen.getByText('에러가 발생했습니다.')).toBeInTheDocument()
    })
  })

  it('should handle page change', async () => {
    const mockData = {
      contents: [
        {
          title: 'Project 1',
          url: '/projects/1',
          imageUrl: 'https://placehold.co/600x400',
          summary: 'Test project 1',
        },
      ],
      totalPages: 5,
    }

    mockFetchMockContents.mockResolvedValue(mockData)

    renderWithRouter(<DashBoard />, { initialEntries: ['/dashboard'] })

    await waitFor(() => {
      expect(screen.getByTestId('project-list')).toBeInTheDocument()
    })

    const nextButton = screen.getByText('Next Page')
    await userEvent.click(nextButton)

    await waitFor(() => {
      expect(mockFetchMockContents).toHaveBeenCalledWith(2, 6)
    })
  })

  it('should fetch data with correct initial parameters', async () => {
    const mockData = {
      contents: [],
      totalPages: 0,
    }

    mockFetchMockContents.mockResolvedValue(mockData)

    renderWithRouter(<DashBoard />, { initialEntries: ['/dashboard'] })

    await waitFor(() => {
      expect(mockFetchMockContents).toHaveBeenCalledWith(1, 6)
    })
  })

  it('should render Todo List section', async () => {
    const mockData = {
      contents: [],
      totalPages: 0,
    }

    mockFetchMockContents.mockResolvedValue(mockData)

    renderWithRouter(<DashBoard />, { initialEntries: ['/dashboard'] })

    await waitFor(() => {
      expect(screen.getByText('Todo List')).toBeInTheDocument()
    })

    const todoSection = screen.getByText('Todo List').closest('div')
    expect(todoSection).toHaveClass('relative', 'flex', 'justify-center')
  })

  it('should render with container padding', async () => {
    const mockData = {
      contents: [],
      totalPages: 0,
    }

    mockFetchMockContents.mockResolvedValue(mockData)

    renderWithRouter(<DashBoard />, { initialEntries: ['/dashboard'] })

    await waitFor(() => {
      const container = screen.getByText('Todo List').closest('.container')
      expect(container).toBeInTheDocument()
    })
  })

  it('should render error state when data is null', async () => {
    mockFetchMockContents.mockResolvedValue(null)

    renderWithRouter(<DashBoard />, { initialEntries: ['/dashboard'] })

    await waitFor(() => {
      expect(screen.getByText('에러가 발생했습니다.')).toBeInTheDocument()
    })
  })

  it('should handle empty contents array', async () => {
    const mockData = {
      contents: [],
      totalPages: 0,
    }

    mockFetchMockContents.mockResolvedValue(mockData)

    renderWithRouter(<DashBoard />, { initialEntries: ['/dashboard'] })

    await waitFor(() => {
      expect(screen.getByTestId('project-list')).toBeInTheDocument()
    })

    expect(screen.getByTestId('contents-count')).toHaveTextContent('0')
  })
})
