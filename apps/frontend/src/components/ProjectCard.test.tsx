import { describe, it, expect } from 'vitest'
import { screen } from '@testing-library/react'
import ProjectCard from './ProjectCard'
import { renderWithRouter } from '@/test-utils/render-with-router'
import type { ProjectInfo } from '@/types'

describe('ProjectCard', () => {
  const mockProject: ProjectInfo = {
    title: 'Test Project',
    url: 'test_project',
    imageUrl: 'https://example.com/image.jpg',
    summary: 'This is a test project summary',
  }

  it('should render project title', () => {
    renderWithRouter(<ProjectCard props={mockProject} />)
    expect(screen.getByText('Test Project')).toBeInTheDocument()
  })

  it('should render project summary', () => {
    renderWithRouter(<ProjectCard props={mockProject} />)
    expect(
      screen.getByText('This is a test project summary')
    ).toBeInTheDocument()
  })

  it('should render project image', () => {
    renderWithRouter(<ProjectCard props={mockProject} />)
    const image = screen.getByAltText('Test Project')
    expect(image).toBeInTheDocument()
    expect(image).toHaveAttribute('src', 'https://example.com/image.jpg')
  })

  it('should have link to project URL', () => {
    renderWithRouter(<ProjectCard props={mockProject} />)
    const link = screen.getByRole('link')
    expect(link).toHaveAttribute('href', '/projects/test_project')
  })
})
