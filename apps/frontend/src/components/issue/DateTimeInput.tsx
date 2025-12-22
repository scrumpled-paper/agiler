import { Calendar as CalendarIcon } from 'lucide-react'

type DateTimeInputProps = {
  label: string
  value: string
  onChange: (value: string) => void
}

export function DateTimeInput({ label, value, onChange }: DateTimeInputProps) {
  return (
    <div className="flex flex-col gap-1">
      <label className="text-sm font-medium text-black">{label}</label>
      <div className="flex h-[38px] items-center justify-between rounded-md border border-[#f1f3f7] bg-white px-3 py-1.5 shadow-sm">
        <input
          type="datetime-local"
          value={value}
          onChange={e => onChange(e.target.value)}
          className="w-full border-none text-xs text-[#6d758f] outline-none"
        />
        <CalendarIcon className="h-4 w-4 text-[#6d758f]" />
      </div>
    </div>
  )
}
