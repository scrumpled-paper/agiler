import { Calendar } from 'lucide-react'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'

interface KanbanDateSelectorProps {
  selectedDate: string
  onDateChange: (date: string) => void
}

export function KanbanDateSelector({
  selectedDate,
  onDateChange,
}: KanbanDateSelectorProps) {
  const generatePastDates = () => {
    const dates = []
    const today = new Date()

    // [ ] 조회 가능 날짜 api 업데이트 후 수정 예정입니다.
    // Generate dates for today and the past 30 days
    for (let i = 0; i < 31; i++) {
      const date = new Date(today)
      date.setDate(today.getDate() - i)

      const dateString = date.toISOString().split('T')[0] // YYYY-MM-DD format
      const displayString =
        i === 0
          ? 'Today'
          : i === 1
            ? 'Yesterday'
            : date.toLocaleDateString('ko-KR', {
                month: 'short',
                day: 'numeric',
                weekday: 'short',
              })

      dates.push({ value: dateString, label: displayString })
    }

    return dates
  }

  const dateOptions = generatePastDates()

  return (
    <Select value={selectedDate} onValueChange={onDateChange}>
      <SelectTrigger className="w-[200px]">
        <Calendar className="mr-2 h-4 w-4" />
        <SelectValue />
      </SelectTrigger>
      <SelectContent className="bg-white max-h-[300px]">
        {dateOptions.map(option => (
          <SelectItem key={option.value} value={option.value}>
            {option.label}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  )
}
