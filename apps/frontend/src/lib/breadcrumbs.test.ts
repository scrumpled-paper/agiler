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
    const breadcrumbs = getBreadcrumbs('/projects/1/dailyscrums', {
      projectUrl: '1',
    })

    expect(breadcrumbs).toHaveLength(3)
    expect(breadcrumbs[0]).toEqual({ label: 'Dashboard', href: '/dashboard' })
    expect(breadcrumbs[1]).toEqual({ label: '1', href: '/projects/1' })
    expect(breadcrumbs[2]).toEqual({
      label: 'Daily Scrum',
      href: '/projects/1/dailyscrums',
    })
  })
  it('should return dashboard > project > Retrospectives', () => {
    const breadcrumbs = getBreadcrumbs('/projects/1/retrospectives', {
      projectUrl: '1',
    })

    expect(breadcrumbs).toHaveLength(3)
    expect(breadcrumbs[0]).toEqual({ label: 'Dashboard', href: '/dashboard' })
    expect(breadcrumbs[1]).toEqual({ label: '1', href: '/projects/1' })
    expect(breadcrumbs[2]).toEqual({
      label: 'Retrospectives',
      href: '/projects/1/retrospectives',
    })
  })
  it('should return dashboard > project > Meetings', () => {
    const breadcrumbs = getBreadcrumbs('/projects/1/meetings', {
      projectUrl: '1',
    })

    expect(breadcrumbs).toHaveLength(3)
    expect(breadcrumbs[0]).toEqual({ label: 'Dashboard', href: '/dashboard' })
    expect(breadcrumbs[1]).toEqual({ label: '1', href: '/projects/1' })
    expect(breadcrumbs[2]).toEqual({
      label: 'Meetings',
      href: '/projects/1/meetings',
    })
  })

  it('should handle missing projectUrl gracefully', () => {
    const breadcrumbs = getBreadcrumbs('/projects/1', {})

    expect(breadcrumbs).toHaveLength(1)
    expect(breadcrumbs[0]).toEqual({ label: 'Dashboard', href: '/dashboard' })
  })
})
