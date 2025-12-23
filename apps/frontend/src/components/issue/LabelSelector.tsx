import { Plus, X } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/components/ui/command'
import type { Label } from '@/types/label'

type LabelSelectorProps = {
  labels: Label[]
  selectedLabels: Label[]
  onAdd: (label: Label) => void
  onRemove: (labelId: Label) => void
  isOpen: boolean
  onOpenChange: (open: boolean) => void
}

export function LabelSelector({
  labels,
  selectedLabels,
  onAdd,
  onRemove,
  isOpen,
  onOpenChange,
}: LabelSelectorProps) {
  return (
    <div className="flex flex-col gap-1">
      <label className="text-sm font-medium text-black">Label</label>
      <div className="flex flex-col gap-2">
        <Popover open={isOpen} onOpenChange={onOpenChange}>
          <PopoverTrigger asChild>
            {/* <div className="flex h-[38px] items-center justify-between rounded-md border border-[#f1f3f7] bg-white px-3 py-1.5 shadow-sm"> */}
            <div className="flex w-4xs h-[38px] items-center justify-between ">
              {/* <span className="text-xs text-[#6d758f]">Select label</span> */}
              <Button
                type="button"
                size="sm"
                className="h-8 bg-[#6d758f] px-3 text-xs hover:bg-[#6d758f]/90"
              >
                <Plus />
              </Button>
            </div>
          </PopoverTrigger>
          <PopoverContent className="p-0 bg-white" align="start">
            <Command>
              <CommandInput placeholder="Search label..." />
              <CommandList>
                <CommandEmpty>No label found.</CommandEmpty>
                <CommandGroup>
                  {labels.map(label => (
                    <CommandItem
                      key={label.labelId}
                      onSelect={() => onAdd(label)}
                      className="flex items-center gap-2"
                    >
                      <div
                        className="h-3 w-3 rounded"
                        style={{ backgroundColor: label.color }}
                      />
                      <span className="text-sm">{label.name}</span>
                    </CommandItem>
                  ))}
                </CommandGroup>
              </CommandList>
            </Command>
          </PopoverContent>
        </Popover>

        {/* Selected Labels */}
        {selectedLabels.length > 0 && (
          <div className="flex flex-wrap gap-1">
            {selectedLabels.map(label => (
              <Badge
                key={label.labelId}
                className="rounded-[10px] px-3 py-1 text-xs text-white"
                style={{ backgroundColor: label.color }}
              >
                {label.name}
                <button
                  type="button"
                  onClick={() => onRemove(label)}
                  className="ml-1 hover:text-gray-200"
                >
                  <X className="h-3 w-3" />
                </button>
              </Badge>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
