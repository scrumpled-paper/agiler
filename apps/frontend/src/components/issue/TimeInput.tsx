import { Clock } from 'lucide-react'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import type { TimeOption } from '@/utils/time-utils'

type TimeInputProps = {
  label: string
  value: string // "HH:mm" 형식
  onChange: (value: string) => void
  options: TimeOption[]
  placeholder?: string
  disabled?: boolean
  emptyMessage?: string
}

export function TimeInput({
  label,
  value,
  onChange,
  options,
  placeholder = '시간 선택',
  disabled = false,
  emptyMessage,
}: TimeInputProps) {
  return (
    <div className="flex flex-1 flex-col gap-1">
      <label className="text-sm font-medium text-black">{label}</label>

      {options.length === 0 && emptyMessage ? (
        <div className="flex h-[38px] items-center rounded-md border border-[#f1f3f7] bg-gray-50 px-3 py-1.5 shadow-sm">
          <span className="text-xs text-[#6d758f]">{emptyMessage}</span>
        </div>
      ) : (
        <Select value={value} onValueChange={onChange} disabled={disabled}>
          <SelectTrigger className="h-[38px] rounded-md border-[#f1f3f7] bg-white shadow-sm">
            <div className="flex items-center gap-2">
              <Clock className="h-4 w-4 text-[#6d758f]" />
              <SelectValue
                placeholder={placeholder}
                className="text-xs text-[#6d758f]"
              />
            </div>
          </SelectTrigger>
          <SelectContent className="max-h-[300px] bg-white">
            {options.map(option => (
              <SelectItem
                key={option.value}
                value={option.value}
                className="text-sm"
              >
                {option.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      )}
    </div>
  )
}
