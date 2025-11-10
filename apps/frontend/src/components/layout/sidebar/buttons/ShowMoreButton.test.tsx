import { describe, it, expect, vi } from 'vitest'
import { screen } from '@testing-library/react'
import { ShowMoreButton } from './ShowMoreButton'
import { renderWithRouter } from '@/test-utils/render-with-router'

// Mock UI button
vi.mock('@/components/ui/button', () => ({
  Button: ({
    children,
    asChild,
  }: {
    children: React.ReactNode
    asChild?: boolean
  }) => (asChild ? <>{children}</> : <button>{children}</button>),
}))

describe('ShowMoreButton', () => {
  it('should render with default label', () => {
    renderWithRouter(<ShowMoreButton to="/projects" />)
    expect(screen.getByText('더보기')).toBeInTheDocument()
  })

  it('should render with custom label', () => {
    renderWithRouter(<ShowMoreButton to="/projects" label="See All" />)
    expect(screen.getByText('See All')).toBeInTheDocument()
  })

  it('should have correct link', () => {
    renderWithRouter(<ShowMoreButton to="/dashboard/projects" />)
    const link = screen.getByRole('link')
    expect(link).toHaveAttribute('href', '/dashboard/projects')
  })
})
