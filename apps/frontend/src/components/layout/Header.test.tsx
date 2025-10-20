// src/components/layout/Header.test.tsx (간소화)
import { describe, it, expect, vi } from 'vitest'
import { screen } from '@testing-library/react'
import { AppHeader } from './Header'
import { renderWithRouter } from '@/test-utils/render-with-router'

vi.mock('@/components/ui/sidebar', () => ({
  SidebarTrigger: () => <button data-testid="sidebar-trigger">Toggle</button>,
}))

describe('AppHeader', () => {
  it('should render header element', () => {
    renderWithRouter(<AppHeader />)
    expect(screen.getByRole('banner')).toBeInTheDocument()
  })

  it('should render sidebar trigger', () => {
    renderWithRouter(<AppHeader />)
    expect(screen.getByTestId('sidebar-trigger')).toBeInTheDocument()
  })

  it('should render breadcrumbs', () => {
    renderWithRouter(<AppHeader />, { initialEntries: ['/dashboard'] })
    expect(screen.getByRole('navigation')).toBeInTheDocument()
  })

  it('should show project actions on project pages', () => {
    renderWithRouter(<AppHeader />, { initialEntries: ['/projects/1'] })
    expect(screen.getByText('Share')).toBeInTheDocument()
  })

  it('should not show project actions on dashboard', () => {
    renderWithRouter(<AppHeader />, { initialEntries: ['/dashboard'] })
    expect(screen.queryByText('Share')).not.toBeInTheDocument()
  })
})
