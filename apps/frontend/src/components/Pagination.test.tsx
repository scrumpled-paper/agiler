import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import DynamicPagination from './Pagination'

// Mock UI pagination components
vi.mock('@/components/ui/pagination', () => ({
  Pagination: ({ children }: { children: React.ReactNode }) => (
    <nav>{children}</nav>
  ),
  PaginationContent: ({ children }: { children: React.ReactNode }) => (
    <div>{children}</div>
  ),
  PaginationItem: ({ children }: { children: React.ReactNode }) => (
    <div>{children}</div>
  ),
  PaginationLink: ({
    children,
    onClick,
    isActive,
  }: {
    children: React.ReactNode
    onClick: (e: React.MouseEvent) => void
    isActive?: boolean
  }) => (
    <button onClick={onClick} data-active={isActive}>
      {children}
    </button>
  ),
  PaginationPrevious: ({
    onClick,
    className,
  }: {
    onClick: (e: React.MouseEvent) => void
    className?: string
  }) => (
    <button onClick={onClick} className={className} data-testid="prev">
      Previous
    </button>
  ),
  PaginationNext: ({
    onClick,
    className,
  }: {
    onClick: (e: React.MouseEvent) => void
    className?: string
  }) => (
    <button onClick={onClick} className={className} data-testid="next">
      Next
    </button>
  ),
}))

describe('DynamicPagination', () => {
  describe('Basic functionality', () => {
    it('should render pagination with correct number of pages', () => {
      const onPageChange = vi.fn()
      render(
        <DynamicPagination
          currentPage={1}
          totalPages={5}
          onPageChange={onPageChange}
        />
      )

      expect(screen.getByText('1')).toBeInTheDocument()
      expect(screen.getByText('5')).toBeInTheDocument()
    })

    it('should call onPageChange when page number is clicked', () => {
      const onPageChange = vi.fn()
      render(
        <DynamicPagination
          currentPage={1}
          totalPages={5}
          onPageChange={onPageChange}
        />
      )

      fireEvent.click(screen.getByText('3'))
      expect(onPageChange).toHaveBeenCalledWith(3)
    })

    it('should call onPageChange when next button is clicked', () => {
      const onPageChange = vi.fn()
      render(
        <DynamicPagination
          currentPage={2}
          totalPages={5}
          onPageChange={onPageChange}
        />
      )

      fireEvent.click(screen.getByTestId('next'))
      expect(onPageChange).toHaveBeenCalledWith(3)
    })

    it('should call onPageChange when previous button is clicked', () => {
      const onPageChange = vi.fn()
      render(
        <DynamicPagination
          currentPage={2}
          totalPages={5}
          onPageChange={onPageChange}
        />
      )

      fireEvent.click(screen.getByTestId('prev'))
      expect(onPageChange).toHaveBeenCalledWith(1)
    })

    it('should disable previous button on first page', () => {
      const onPageChange = vi.fn()
      render(
        <DynamicPagination
          currentPage={1}
          totalPages={5}
          onPageChange={onPageChange}
        />
      )

      const prevButton = screen.getByTestId('prev')
      expect(prevButton.className).toContain('opacity-50')
    })

    it('should disable next button on last page', () => {
      const onPageChange = vi.fn()
      render(
        <DynamicPagination
          currentPage={5}
          totalPages={5}
          onPageChange={onPageChange}
        />
      )

      const nextButton = screen.getByTestId('next')
      expect(nextButton.className).toContain('opacity-50')
    })

    it('should not call onPageChange when previous button is clicked on first page', () => {
      const onPageChange = vi.fn()
      render(
        <DynamicPagination
          currentPage={1}
          totalPages={5}
          onPageChange={onPageChange}
        />
      )

      fireEvent.click(screen.getByTestId('prev'))
      expect(onPageChange).not.toHaveBeenCalled()
    })

    it('should not call onPageChange when next button is clicked on last page', () => {
      const onPageChange = vi.fn()
      render(
        <DynamicPagination
          currentPage={5}
          totalPages={5}
          onPageChange={onPageChange}
        />
      )

      fireEvent.click(screen.getByTestId('next'))
      expect(onPageChange).not.toHaveBeenCalled()
    })
  })

  describe('Dynamic pagination (5 or fewer pages)', () => {
    it('should show all pages when totalPages is 5 or less', () => {
      const onPageChange = vi.fn()
      render(
        <DynamicPagination
          currentPage={3}
          totalPages={5}
          onPageChange={onPageChange}
        />
      )

      expect(screen.getByText('1')).toBeInTheDocument()
      expect(screen.getByText('2')).toBeInTheDocument()
      expect(screen.getByText('3')).toBeInTheDocument()
      expect(screen.getByText('4')).toBeInTheDocument()
      expect(screen.getByText('5')).toBeInTheDocument()
    })

    it('should not show ellipsis when totalPages is 5 or less', () => {
      const onPageChange = vi.fn()
      render(
        <DynamicPagination
          currentPage={1}
          totalPages={5}
          onPageChange={onPageChange}
        />
      )

      expect(screen.queryByText('...')).not.toBeInTheDocument()
    })
  })

  describe('Dynamic pagination (more than 5 pages)', () => {
    it('should show ellipsis and last page when current page is near start', () => {
      const onPageChange = vi.fn()
      render(
        <DynamicPagination
          currentPage={2}
          totalPages={10}
          onPageChange={onPageChange}
        />
      )

      // Should show pages 1-5
      expect(screen.getByText('1')).toBeInTheDocument()
      expect(screen.getByText('2')).toBeInTheDocument()
      expect(screen.getByText('3')).toBeInTheDocument()
      expect(screen.getByText('4')).toBeInTheDocument()
      expect(screen.getByText('5')).toBeInTheDocument()

      // Should show ellipsis and page 10
      expect(screen.getByText('...')).toBeInTheDocument()
      expect(screen.getByText('10')).toBeInTheDocument()

      // Should not show page 6, 7, 8, 9
      expect(screen.queryByText('6')).not.toBeInTheDocument()
    })

    it('should show first page, ellipsis when current page is near end', () => {
      const onPageChange = vi.fn()
      render(
        <DynamicPagination
          currentPage={9}
          totalPages={10}
          onPageChange={onPageChange}
        />
      )

      // Should show page 1 and ellipsis
      expect(screen.getByText('1')).toBeInTheDocument()
      expect(screen.getByText('...')).toBeInTheDocument()

      // Should show pages 6-10
      expect(screen.getByText('6')).toBeInTheDocument()
      expect(screen.getByText('7')).toBeInTheDocument()
      expect(screen.getByText('8')).toBeInTheDocument()
      expect(screen.getByText('9')).toBeInTheDocument()
      expect(screen.getByText('10')).toBeInTheDocument()
    })

    it('should show both ellipses when current page is in the middle', () => {
      const onPageChange = vi.fn()
      render(
        <DynamicPagination
          currentPage={6}
          totalPages={10}
          onPageChange={onPageChange}
        />
      )

      // Should show page 1
      expect(screen.getByText('1')).toBeInTheDocument()

      // Should show middle pages (4, 5, 6, 7, 8)
      expect(screen.getByText('4')).toBeInTheDocument()
      expect(screen.getByText('5')).toBeInTheDocument()
      expect(screen.getByText('6')).toBeInTheDocument()
      expect(screen.getByText('7')).toBeInTheDocument()
      expect(screen.getByText('8')).toBeInTheDocument()

      // Should show page 10
      expect(screen.getByText('10')).toBeInTheDocument()

      // Should show two ellipses
      const ellipses = screen.getAllByText('...')
      expect(ellipses).toHaveLength(2)
    })

    it('should allow clicking on first page button when not on first page', () => {
      const onPageChange = vi.fn()
      render(
        <DynamicPagination
          currentPage={9}
          totalPages={10}
          onPageChange={onPageChange}
        />
      )

      const firstPageButtons = screen.getAllByText('1')
      fireEvent.click(firstPageButtons[0])
      expect(onPageChange).toHaveBeenCalledWith(1)
    })

    it('should allow clicking on last page button when not on last page', () => {
      const onPageChange = vi.fn()
      render(
        <DynamicPagination
          currentPage={2}
          totalPages={10}
          onPageChange={onPageChange}
        />
      )

      const lastPageButtons = screen.getAllByText('10')
      fireEvent.click(lastPageButtons[0])
      expect(onPageChange).toHaveBeenCalledWith(10)
    })

    it('should not show ellipsis between page 1 and page 2', () => {
      const onPageChange = vi.fn()
      render(
        <DynamicPagination
          currentPage={3}
          totalPages={10}
          onPageChange={onPageChange}
        />
      )

      // Should show pages 1, 2, 3, 4, 5
      expect(screen.getByText('1')).toBeInTheDocument()
      expect(screen.getByText('2')).toBeInTheDocument()
      expect(screen.getByText('3')).toBeInTheDocument()
      expect(screen.getByText('4')).toBeInTheDocument()
      expect(screen.getByText('5')).toBeInTheDocument()

      // Should only show one ellipsis (after 5)
      const ellipses = screen.getAllByText('...')
      expect(ellipses).toHaveLength(1)
    })

    it('should not show ellipsis between page 9 and page 10', () => {
      const onPageChange = vi.fn()
      render(
        <DynamicPagination
          currentPage={8}
          totalPages={10}
          onPageChange={onPageChange}
        />
      )

      // Should show pages 6, 7, 8, 9, 10
      expect(screen.getByText('6')).toBeInTheDocument()
      expect(screen.getByText('7')).toBeInTheDocument()
      expect(screen.getByText('8')).toBeInTheDocument()
      expect(screen.getByText('9')).toBeInTheDocument()
      expect(screen.getByText('10')).toBeInTheDocument()

      // Should only show one ellipsis (before 6)
      const ellipses = screen.getAllByText('...')
      expect(ellipses).toHaveLength(1)
    })
  })

  describe('Active page indicator', () => {
    it('should mark current page as active', () => {
      const onPageChange = vi.fn()
      render(
        <DynamicPagination
          currentPage={3}
          totalPages={5}
          onPageChange={onPageChange}
        />
      )

      const buttons = screen.getAllByRole('button')
      const page3Button = buttons.find(
        btn =>
          btn.textContent === '3' && btn.getAttribute('data-active') === 'true'
      )

      expect(page3Button).toBeDefined()
    })
  })
})
