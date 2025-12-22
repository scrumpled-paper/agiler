import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import {
  generateTimeIntervals,
  timeToISO,
  isoToTime,
  getAvailableStartTimes,
  getAvailableEndTimes,
} from './time-utils'

describe('generateTimeIntervals', () => {
  it('30분 간격으로 48개의 시간 옵션을 생성한다', () => {
    const intervals = generateTimeIntervals()
    expect(intervals).toHaveLength(48)
  })

  it('첫 번째 옵션은 00:00 (오전 12:00)이다', () => {
    const intervals = generateTimeIntervals()
    expect(intervals[0]).toEqual({ value: '00:00', label: '오전 12:00' })
  })

  it('마지막 옵션은 23:30 (오후 11:30)이다', () => {
    const intervals = generateTimeIntervals()
    expect(intervals[intervals.length - 1]).toEqual({
      value: '23:30',
      label: '오후 11:30',
    })
  })

  it('정오는 오후 12:00으로 표시된다', () => {
    const intervals = generateTimeIntervals()
    const noon = intervals.find(t => t.value === '12:00')
    expect(noon?.label).toBe('오후 12:00')
  })

  it('자정은 오전 12:00으로 표시된다', () => {
    const intervals = generateTimeIntervals()
    const midnight = intervals.find(t => t.value === '00:00')
    expect(midnight?.label).toBe('오전 12:00')
  })

  it('오전 시간은 "오전" 접두사를 가진다', () => {
    const intervals = generateTimeIntervals()
    const morning = intervals.find(t => t.value === '09:30')
    expect(morning?.label).toBe('오전 9:30')
  })

  it('오후 시간은 "오후" 접두사를 가진다', () => {
    const intervals = generateTimeIntervals()
    const afternoon = intervals.find(t => t.value === '14:30')
    expect(afternoon?.label).toBe('오후 2:30')
  })
})

describe('timeToISO', () => {
  it('"14:30"을 오늘 날짜의 ISO 8601 형식으로 변환한다', () => {
    const result = timeToISO('14:30')
    const date = new Date(result)

    expect(date.getHours()).toBe(14)
    expect(date.getMinutes()).toBe(30)
    expect(date.getSeconds()).toBe(0)
  })

  it('빈 문자열은 빈 문자열을 반환한다', () => {
    expect(timeToISO('')).toBe('')
  })

  it('"00:00"을 자정의 ISO 8601 형식으로 변환한다', () => {
    const result = timeToISO('00:00')
    const date = new Date(result)

    expect(date.getHours()).toBe(0)
    expect(date.getMinutes()).toBe(0)
  })

  it('"23:30"을 올바른 ISO 8601 형식으로 변환한다', () => {
    const result = timeToISO('23:30')
    const date = new Date(result)

    expect(date.getHours()).toBe(23)
    expect(date.getMinutes()).toBe(30)
  })
})

describe('isoToTime', () => {
  it('ISO 8601 형식을 "HH:mm" 형식으로 변환한다', () => {
    // 로컬 시간으로 14:30를 생성
    const date = new Date()
    date.setHours(14, 30, 0, 0)
    const iso = date.toISOString()

    const result = isoToTime(iso)

    expect(result).toBe('14:30')
  })

  it('빈 문자열은 빈 문자열을 반환한다', () => {
    expect(isoToTime('')).toBe('')
  })

  it('자정(00:00)을 올바르게 변환한다', () => {
    const date = new Date()
    date.setHours(0, 0, 0, 0)
    const iso = date.toISOString()

    const result = isoToTime(iso)

    expect(result).toBe('00:00')
  })
})

describe('getAvailableStartTimes', () => {
  beforeEach(() => {
    // 가짜 타이머 사용
    vi.useFakeTimers()
  })

  afterEach(() => {
    // 실제 타이머로 복원
    vi.useRealTimers()
  })

  it('현재 시간(14:15) 이후의 시간만 반환한다 (다음 슬롯: 14:30)', () => {
    vi.setSystemTime(new Date('2025-12-22T14:15:00'))
    const times = getAvailableStartTimes()

    const firstTime = times[0]
    expect(firstTime.value).toBe('14:30')
  })

  it('현재 시간(14:30) 정각이면 다음 슬롯(15:00)부터 반환한다', () => {
    vi.setSystemTime(new Date('2025-12-22T14:30:00'))
    const times = getAvailableStartTimes()

    const firstTime = times[0]
    expect(firstTime.value).toBe('15:00')
  })

  it('현재 시간(14:45)이면 다음 슬롯(15:00)부터 반환한다', () => {
    vi.setSystemTime(new Date('2025-12-22T14:45:00'))
    const times = getAvailableStartTimes()

    const firstTime = times[0]
    expect(firstTime.value).toBe('15:00')
  })

  it('23:45 이후에는 빈 배열을 반환한다', () => {
    vi.setSystemTime(new Date('2025-12-22T23:45:00'))
    const times = getAvailableStartTimes()

    expect(times).toHaveLength(0)
  })

  it('23:30 정각이면 빈 배열을 반환한다', () => {
    vi.setSystemTime(new Date('2025-12-22T23:30:00'))
    const times = getAvailableStartTimes()

    expect(times).toHaveLength(0)
  })

  it('오전 9시(09:00)에는 09:30부터 반환한다', () => {
    vi.setSystemTime(new Date('2025-12-22T09:00:00'))
    const times = getAvailableStartTimes()

    const firstTime = times[0]
    expect(firstTime.value).toBe('09:30')
    expect(times.length).toBeGreaterThan(0)
  })
})

describe('getAvailableEndTimes', () => {
  it('시작 시간(14:30) 이후의 시간만 반환한다', () => {
    const times = getAvailableEndTimes('14:30')

    expect(
      times.every(t => {
        const [hour, minute] = t.value.split(':').map(Number)
        return hour > 14 || (hour === 14 && minute > 30)
      })
    ).toBe(true)
  })

  it('시작 시간이 비어있으면 빈 배열을 반환한다', () => {
    const times = getAvailableEndTimes('')
    expect(times).toHaveLength(0)
  })

  it('23:30을 시작 시간으로 설정하면 빈 배열을 반환한다', () => {
    const times = getAvailableEndTimes('23:30')
    expect(times).toHaveLength(0)
  })

  it('23:00을 시작 시간으로 설정하면 23:30만 반환한다', () => {
    const times = getAvailableEndTimes('23:00')
    expect(times).toHaveLength(1)
    expect(times[0].value).toBe('23:30')
  })

  it('00:00을 시작 시간으로 설정하면 00:30부터 반환한다', () => {
    const times = getAvailableEndTimes('00:00')
    expect(times[0].value).toBe('00:30')
    expect(times.length).toBe(47) // 00:30부터 23:30까지
  })

  it('14:00을 시작 시간으로 설정하면 14:30부터 반환한다', () => {
    const times = getAvailableEndTimes('14:00')
    const firstTime = times[0]
    expect(firstTime.value).toBe('14:30')
  })
})
