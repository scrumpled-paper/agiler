// 시간 선택 옵션 타입
export type TimeOption = {
  value: string // "HH:mm" 형식
  label: string // 표시용 라벨 (예: "오전 2:30")
}

/**
 * 30분 간격으로 00:00부터 23:30까지의 시간 옵션을 생성합니다.
 * @returns TimeOption 배열 (총 48개)
 */
export function generateTimeIntervals(): TimeOption[] {
  const times: TimeOption[] = []

  for (let hour = 0; hour < 24; hour++) {
    for (let minute = 0; minute < 60; minute += 30) {
      const timeValue = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
      const period = hour < 12 ? '오전' : '오후'
      const displayHour = hour === 0 ? 12 : hour > 12 ? hour - 12 : hour
      const label = `${period} ${displayHour}:${minute.toString().padStart(2, '0')}`

      times.push({ value: timeValue, label })
    }
  }

  return times
}

/**
 * 현재 시간 이후의 시작 시간 옵션만 반환합니다.
 * 현재 시간을 다음 30분 단위로 올림하여 계산합니다.
 * @returns 선택 가능한 TimeOption 배열
 */
export function getAvailableStartTimes(): TimeOption[] {
  const now = new Date()
  const currentHour = now.getHours()
  const currentMinute = now.getMinutes()

  // 현재 시간을 30분 단위로 올림
  const roundedMinute = currentMinute < 30 ? 30 : 0
  const roundedHour = roundedMinute === 0 ? currentHour + 1 : currentHour

  // 자정을 넘어가면 빈 배열 반환
  if (roundedHour >= 24) {
    return []
  }

  const allTimes = generateTimeIntervals()

  // 반올림된 시간 이후의 옵션만 반환
  return allTimes.filter(time => {
    const [hour, minute] = time.value.split(':').map(Number)
    return (
      hour > roundedHour || (hour === roundedHour && minute >= roundedMinute)
    )
  })
}

/**
 * 주어진 시작 시간 이후의 마감 시간 옵션만 반환합니다.
 * @param startTime - "HH:mm" 형식의 시작 시간
 * @returns 선택 가능한 TimeOption 배열
 */
export function getAvailableEndTimes(startTime: string): TimeOption[] {
  if (!startTime) return []

  const [startHour, startMinute] = startTime.split(':').map(Number)
  const allTimes = generateTimeIntervals()

  return allTimes.filter(time => {
    const [hour, minute] = time.value.split(':').map(Number)
    return hour > startHour || (hour === startHour && minute > startMinute)
  })
}

/**
 * "HH:mm" 형식의 시간을 오늘 날짜의 ISO 8601 형식으로 변환합니다.
 * @param timeString - "14:30" 형식의 시간 문자열
 * @returns ISO 8601 형식 문자열 (예: "2025-12-22T14:30:00.000Z")
 */
export function timeToISO(timeString: string): string {
  if (!timeString) return ''

  const today = new Date()
  const [hours, minutes] = timeString.split(':').map(Number)

  today.setHours(hours, minutes, 0, 0)
  return today.toISOString()
}

/**
 * ISO 8601 형식 문자열을 "HH:mm" 형식으로 변환합니다.
 * @param isoString - ISO 8601 형식 문자열
 * @returns "HH:mm" 형식 문자열 (예: "14:30")
 */
export function isoToTime(isoString: string): string {
  if (!isoString) return ''

  const date = new Date(isoString)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')

  return `${hours}:${minutes}`
}
