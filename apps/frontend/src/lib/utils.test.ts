import { describe, it, expect } from 'vitest'
import { cn } from './utils'

describe('cn utility', () => {
  it('should merge class names', () => {
    expect(cn('class1', 'class2')).toBe('class1 class2')
  })

  it('should handle conditional classes', () => {
    expect(
      cn('base', {
        active: true, // true일 때 'active' 클래스 적용
        disabled: false, // false일 때 'disabled' 클래스 미적용
      })
    ).toBe('base active')
  })

  it('should merge tailwind classes without conflicts', () => {
    // tailwind-merge should handle conflicting classes
    expect(cn('px-2 py-1', 'px-4')).toBe('py-1 px-4')
  })

  it('should handle undefined and null', () => {
    expect(cn('class1', undefined, null, 'class2')).toBe('class1 class2')
  })

  it('should handle empty input', () => {
    expect(cn()).toBe('')
  })
})
