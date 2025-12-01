import { describe, it, expect } from 'vitest'
import { getSidebarContext, sidebarConfigs } from './config'

describe('getSidebarContext', () => {
  it('should return "dashboard" for dashboard paths', () => {
    expect(getSidebarContext('/dashboard')).toBe('dashboard')
    expect(getSidebarContext('/dashboard/projects')).toBe('dashboard')
  })

  it('should return "project" for project paths', () => {
    expect(getSidebarContext('/projects/1')).toBe('project')
    expect(getSidebarContext('/projects/my-project')).toBe('project')
    expect(getSidebarContext('/projects/1/daily-scrum')).toBe('project')
  })

  it('should return "project-settings" for settings paths', () => {
    expect(getSidebarContext('/projects/1/settings')).toBe('project-settings')
    expect(getSidebarContext('/projects/1/settings/profile')).toBe(
      'project-settings'
    )
    expect(getSidebarContext('/dashboard/settings')).toBe('project-settings')
  })

  it('should return "dashboard" for unknown paths', () => {
    expect(getSidebarContext('/')).toBe('dashboard')
    expect(getSidebarContext('/unknown')).toBe('dashboard')
  })
})

describe('sidebarConfigs', () => {
  it('should have configuration for all contexts', () => {
    expect(sidebarConfigs.dashboard).toBeDefined()
    expect(sidebarConfigs.project).toBeDefined()
    expect(sidebarConfigs['project-settings']).toBeDefined()
  })

  it('should have user-info header for all contexts', () => {
    expect(sidebarConfigs.dashboard.header?.type).toBe('user-info')
    expect(sidebarConfigs.project.header?.type).toBe('user-info')
  })

  it('should have sections array for all contexts', () => {
    expect(Array.isArray(sidebarConfigs.dashboard.sections)).toBe(true)
    expect(Array.isArray(sidebarConfigs.project.sections)).toBe(true)
    expect(Array.isArray(sidebarConfigs['project-settings'].sections)).toBe(
      true
    )
  })
})
