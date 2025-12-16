import { describe, it, expect } from 'vitest'
import { formatDate } from './date-formatter'

describe('formatDate', () => {
  describe('Date 객체 입력', () => {
    it('Date 객체를 한국어 형식으로 변환한다', () => {
      const date = new Date('2024-01-15T10:00:00')
      expect(formatDate(date)).toBe('2024년 1월 15일')
    })

    it('다양한 월의 Date 객체를 올바르게 변환한다', () => {
      const marchDate = new Date('2024-03-20T15:30:00')
      expect(formatDate(marchDate)).toBe('2024년 3월 20일')

      const decemberDate = new Date('2024-12-25T00:00:00')
      expect(formatDate(decemberDate)).toBe('2024년 12월 25일')
    })
  })

  describe('문자열 입력', () => {
    it('ISO 문자열을 한국어 형식으로 변환한다', () => {
      expect(formatDate('2024-01-15T10:00:00')).toBe('2024년 1월 15일')
    })

    it('다양한 ISO 문자열 형식을 처리한다', () => {
      expect(formatDate('2024-06-30T23:59:59')).toBe('2024년 6월 30일')
      expect(formatDate('2024-02-14T09:00:00')).toBe('2024년 2월 14일')
    })
  })

  describe('다양한 날짜 케이스', () => {
    it('연초 날짜를 올바르게 처리한다', () => {
      expect(formatDate('2024-01-01T00:00:00')).toBe('2024년 1월 1일')
    })

    it('연말 날짜를 올바르게 처리한다', () => {
      expect(formatDate('2024-12-31T23:59:59')).toBe('2024년 12월 31일')
    })

    it('윤년 날짜를 올바르게 처리한다', () => {
      expect(formatDate('2024-02-29T12:00:00')).toBe('2024년 2월 29일')
    })
  })

  describe('엣지 케이스', () => {
    it('한 자리 월과 일을 올바르게 표시한다', () => {
      expect(formatDate('2024-01-01T00:00:00')).toBe('2024년 1월 1일')
      expect(formatDate('2024-05-09T00:00:00')).toBe('2024년 5월 9일')
    })

    it('두 자리 월과 일을 올바르게 표시한다', () => {
      expect(formatDate('2024-11-30T00:00:00')).toBe('2024년 11월 30일')
    })
  })
})
