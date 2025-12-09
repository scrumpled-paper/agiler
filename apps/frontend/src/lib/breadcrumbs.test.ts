import { describe, it, expect } from 'vitest'
import { getBreadcrumbs } from './breadcrumbs'

describe('getBreadcrumbs', () => {
  it('should return dashboard breadcrumb for dashboard page', () => {
    const breadcrumbs = getBreadcrumbs('/dashboard', {})

    expect(breadcrumbs).toHaveLength(1)
    expect(breadcrumbs[0]).toEqual({ label: 'Dashboard', href: '/dashboard' })
  })

  it('should return dashboard > settings for dashboard settings', () => {
    const breadcrumbs = getBreadcrumbs('/dashboard/settings', {})

    expect(breadcrumbs).toHaveLength(2)
    expect(breadcrumbs[0]).toEqual({ label: 'Dashboard', href: '/dashboard' })
    expect(breadcrumbs[1]).toEqual({ label: 'Settings' })
  })

  it('should return dashboard > project for project page', () => {
    const breadcrumbs = getBreadcrumbs('/projects/1', { projectUrl: '1' })

    expect(breadcrumbs).toHaveLength(2)
    expect(breadcrumbs[0]).toEqual({ label: 'Dashboard', href: '/dashboard' })
    expect(breadcrumbs[1]).toEqual({ label: '1', href: '/projects/1' })
  })

  it('should return dashboard > project > settings', () => {
    const breadcrumbs = getBreadcrumbs('/projects/1/settings', {
      projectUrl: '1',
    })

    expect(breadcrumbs).toHaveLength(3)
    expect(breadcrumbs[0]).toEqual({ label: 'Dashboard', href: '/dashboard' })
    expect(breadcrumbs[1]).toEqual({ label: '1', href: '/projects/1' })
    expect(breadcrumbs[2]).toEqual({ label: 'Settings' })
  })

  it('should return dashboard > project > daily scrum', () => {
    const breadcrumbs = getBreadcrumbs('/projects/1/daily-scrum', {
      projectUrl: '1',
    })

    expect(breadcrumbs).toHaveLength(3)
    expect(breadcrumbs[0]).toEqual({ label: 'Dashboard', href: '/dashboard' })
    expect(breadcrumbs[1]).toEqual({ label: '1', href: '/projects/1' })
    expect(breadcrumbs[2]).toEqual({
      label: 'Daily Scrum',
      href: '/projects/1/daily-scrum',
    })
  })

  it('should return full breadcrumb for specific scrum', () => {
    const breadcrumbs = getBreadcrumbs('/projects/1/daily-scrum/123', {
      projectUrl: '1',
      scrumId: '123',
    })

    expect(breadcrumbs).toHaveLength(4)
    expect(breadcrumbs[0]).toEqual({ label: 'Dashboard', href: '/dashboard' })
    expect(breadcrumbs[1]).toEqual({ label: '1', href: '/projects/1' })
    expect(breadcrumbs[2]).toEqual({
      label: 'Daily Scrum',
      href: '/projects/1/daily-scrum',
    })
    expect(breadcrumbs[3]).toEqual({ label: 'Scrum #123' })
  })

  it('should handle missing projectUrl gracefully', () => {
    const breadcrumbs = getBreadcrumbs('/projects/1', {})

    expect(breadcrumbs).toHaveLength(1)
    expect(breadcrumbs[0]).toEqual({ label: 'Dashboard', href: '/dashboard' })
  })
})
