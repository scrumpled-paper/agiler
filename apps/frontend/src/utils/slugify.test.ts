import { describe, it, expect } from 'vitest'
import { slugify, isValidSlug } from './slugify'

describe('slugify', () => {
  it('should convert Korean to romanized slug', () => {
    expect(slugify('프로젝트 관리')).toBe('peurojegteu-gwanri')
    expect(slugify('애자일 개발')).toBe('aejair-gaebar')
  })

  it('should convert English text to lowercase slug', () => {
    expect(slugify('My Project')).toBe('my-project')
    expect(slugify('Hello World')).toBe('hello-world')
  })

  it('should handle mixed Korean and English', () => {
    expect(slugify('Project 관리')).toBe('project-gwanri')
    expect(slugify('애자일 Agile')).toBe('aejair-agile')
  })

  it('should handle numbers', () => {
    expect(slugify('프로젝트 123')).toBe('peurojegteu-123')
    expect(slugify('My Project 2024')).toBe('my-project-2024')
  })

  it('should remove special characters', () => {
    expect(slugify('Hello@World!')).toBe('helloworld')
    expect(slugify('프로젝트#1')).toBe('peurojegteu1')
  })

  it('should handle multiple spaces and separators', () => {
    expect(slugify('hello   world')).toBe('hello-world')
    expect(slugify('hello---world')).toBe('hello-world')
  })

  it('should trim separators from start and end', () => {
    expect(slugify(' hello world ')).toBe('hello-world')
    expect(slugify('-hello-world-')).toBe('hello-world')
  })

  it('should respect maxLength option', () => {
    const result = slugify('very long project name that exceeds limit', {
      maxLength: 20,
    })
    expect(result.length).toBeLessThanOrEqual(20)
    // Actual output is 'very-long-project-na' which is 20 chars
    expect(result).toMatch(/^very-long-project/)
  })

  it('should respect custom separator', () => {
    expect(slugify('hello world', { separator: '_' })).toBe('hello_world')
  })

  it('should keep uppercase when lowercase is false', () => {
    expect(slugify('Hello World', { lowercase: false })).toBe('Hello-World')
  })
})

describe('isValidSlug', () => {
  it('should return true for valid slugs', () => {
    expect(isValidSlug('valid-slug')).toBe(true)
    expect(isValidSlug('project-123')).toBe(true)
    expect(isValidSlug('my-project-name')).toBe(true)
  })

  it('should return false for invalid slugs', () => {
    expect(isValidSlug('Invalid-Slug')).toBe(false) // uppercase
    expect(isValidSlug('-invalid')).toBe(false) // starts with hyphen
    expect(isValidSlug('invalid-')).toBe(false) // ends with hyphen
    expect(isValidSlug('invalid--slug')).toBe(false) // double hyphen
    expect(isValidSlug('invalid slug')).toBe(false) // space
    expect(isValidSlug('')).toBe(false) // empty
  })
})
