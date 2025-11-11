import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import PaginationDemo from './Pagination'

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

describe('PaginationDemo', () => {
  it('should render pagination with correct number of pages', () => {
    const onPageChange = vi.fn()
    render(
      <PaginationDemo
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
      <PaginationDemo
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
      <PaginationDemo
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
      <PaginationDemo
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
      <PaginationDemo
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
      <PaginationDemo
        currentPage={5}
        totalPages={5}
        onPageChange={onPageChange}
      />
    )

    const nextButton = screen.getByTestId('next')
    expect(nextButton.className).toContain('opacity-50')
  })
})
