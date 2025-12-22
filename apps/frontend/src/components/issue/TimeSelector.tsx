import { useMemo } from 'react'
import { TimeInput } from './TimeInput'
import {
  getAvailableStartTimes,
  getAvailableEndTimes,
  timeToISO,
  isoToTime,
} from '@/utils/time-utils'

type TimeSelectorProps = {
  startedAt: string // ISO 8601 형식
  dueAt: string // ISO 8601 형식
  setStartedAt: (value: string) => void
  setDueAt: (value: string) => void
}

export function TimeSelector({
  startedAt,
  dueAt,
  setStartedAt,
  setDueAt,
}: TimeSelectorProps) {
  // ISO 형식 <-> "HH:mm" 형식 변환
  const startTime = useMemo(() => isoToTime(startedAt), [startedAt])
  const endTime = useMemo(() => isoToTime(dueAt), [dueAt])

  // 사용 가능한 시간 옵션 계산
  const startTimeOptions = useMemo(() => getAvailableStartTimes(), [])
  const endTimeOptions = useMemo(
    () => getAvailableEndTimes(startTime),
    [startTime]
  )

  // 시작 시간 변경 핸들러
  const handleStartTimeChange = (time: string) => {
    const isoString = timeToISO(time)
    setStartedAt(isoString)

    // 시작 시간이 마감 시간 이후로 변경되면 마감 시간 초기화
    if (endTime && time >= endTime) {
      setDueAt('')
    }
  }

  // 마감 시간 변경 핸들러
  const handleEndTimeChange = (time: string) => {
    const isoString = timeToISO(time)
    setDueAt(isoString)
  }

  return (
    <div className="flex items-start justify-between gap-4">
      <TimeInput
        label="시작 시간"
        value={startTime}
        onChange={handleStartTimeChange}
        options={startTimeOptions}
        placeholder="시작 시간 선택"
        emptyMessage="오늘은 시작 가능한 시간이 없습니다"
      />
      <TimeInput
        label="마감 시간"
        value={endTime}
        onChange={handleEndTimeChange}
        options={endTimeOptions}
        placeholder="마감 시간 선택"
        disabled={!startTime}
        emptyMessage={
          !startTime
            ? '시작 시간을 먼저 선택하세요'
            : '선택 가능한 시간이 없습니다'
        }
      />
    </div>
  )
}
