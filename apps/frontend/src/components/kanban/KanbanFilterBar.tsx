import { Search, X } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/components/ui/command'
import { Input } from '@/components/ui/input'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import type { Label, UserInfo } from '@/types'

export type SortOption =
  | 'endAt-asc'
  | 'endAt-desc'
  | 'startAt-asc'
  | 'startAt-desc'
  | 'name-asc'

export interface KanbanFilters {
  search: string
  sortBy: SortOption
  selectedOwners: string[] // nickname[]
  selectedLabels: string[] // label names[]
  selectedSubscribers: string[] // nickname[]
}

interface KanbanFilterBarProps {
  filters: KanbanFilters
  onFiltersChange: (filters: KanbanFilters) => void
  availableOwners: UserInfo[]
  availableLabels: Label[]
  availableSubscribers: UserInfo[]
}

export function KanbanFilterBar({
  filters,
  onFiltersChange,
  availableOwners,
  availableLabels,
  availableSubscribers,
}: KanbanFilterBarProps) {
  const updateFilters = (updates: Partial<KanbanFilters>) => {
    onFiltersChange({ ...filters, ...updates })
  }

  const toggleOwner = (nickname: string) => {
    const newOwners = filters.selectedOwners.includes(nickname)
      ? filters.selectedOwners.filter(n => n !== nickname)
      : [...filters.selectedOwners, nickname]
    updateFilters({ selectedOwners: newOwners })
  }

  const toggleLabel = (labelName: string) => {
    const newLabels = filters.selectedLabels.includes(labelName)
      ? filters.selectedLabels.filter(n => n !== labelName)
      : [...filters.selectedLabels, labelName]
    updateFilters({ selectedLabels: newLabels })
  }

  const toggleSubscriber = (nickname: string) => {
    const newSubscribers = filters.selectedSubscribers.includes(nickname)
      ? filters.selectedSubscribers.filter(n => n !== nickname)
      : [...filters.selectedSubscribers, nickname]
    updateFilters({ selectedSubscribers: newSubscribers })
  }

  const clearAllFilters = () => {
    onFiltersChange({
      search: '',
      sortBy: 'endAt-asc',
      selectedOwners: [],
      selectedLabels: [],
      selectedSubscribers: [],
    })
  }

  const hasActiveFilters =
    filters.search ||
    filters.selectedOwners.length > 0 ||
    filters.selectedLabels.length > 0 ||
    filters.selectedSubscribers.length > 0

  return (
    <div className="space-y-3 rounded-lg border bg-card p-4">
      <div className="flex flex-wrap items-center gap-3">
        {/* Search */}
        <div className="relative flex-1 min-w-[200px] ">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="이슈 검색..."
            value={filters.search}
            onChange={e => updateFilters({ search: e.target.value })}
            className="pl-9"
          />
        </div>

        {/* Sort */}
        <Select
          value={filters.sortBy}
          onValueChange={value =>
            updateFilters({ sortBy: value as SortOption })
          }
        >
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="정렬 기준" />
          </SelectTrigger>
          <SelectContent className="bg-white">
            <SelectItem value="endAt-asc">마감일 빠른순</SelectItem>
            <SelectItem value="endAt-desc">마감일 느린순</SelectItem>
            <SelectItem value="startAt-asc">시작일 빠른순</SelectItem>
            <SelectItem value="startAt-desc">시작일 느린순</SelectItem>
            <SelectItem value="name-asc">이름순 (A-Z)</SelectItem>
          </SelectContent>
        </Select>

        {/* Owner Filter */}
        <Popover>
          <PopoverTrigger asChild>
            <Button variant="outline" className="gap-2">
              담당자
              {filters.selectedOwners.length > 0 && (
                <Badge variant="secondary" className="ml-1 px-1.5 py-0">
                  {filters.selectedOwners.length}
                </Badge>
              )}
            </Button>
          </PopoverTrigger>
          <PopoverContent className="w-[200px] p-0 bg-white" align="start">
            <Command>
              <CommandInput placeholder="담당자 검색..." />
              <CommandList>
                <CommandEmpty>결과 없음</CommandEmpty>
                <CommandGroup>
                  {availableOwners.map(owner => (
                    <CommandItem
                      key={owner.nickname}
                      onSelect={() => toggleOwner(owner.nickname)}
                    >
                      <div className="flex items-center gap-2 ">
                        <div
                          className={`h-4 w-4 rounded border ${
                            filters.selectedOwners.includes(owner.nickname)
                              ? 'bg-primary border-primary'
                              : 'border-input'
                          }`}
                        />
                        <span>{owner.nickname}</span>
                      </div>
                    </CommandItem>
                  ))}
                </CommandGroup>
              </CommandList>
            </Command>
          </PopoverContent>
        </Popover>

        {/* Label Filter */}
        <Popover>
          <PopoverTrigger asChild>
            <Button variant="outline" className="gap-2">
              라벨
              {filters.selectedLabels.length > 0 && (
                <Badge variant="secondary" className="ml-1 px-1.5 py-0">
                  {filters.selectedLabels.length}
                </Badge>
              )}
            </Button>
          </PopoverTrigger>
          <PopoverContent className="w-[200px] p-0 bg-white" align="start">
            <Command>
              <CommandInput placeholder="라벨 검색..." />
              <CommandList>
                <CommandEmpty>결과 없음</CommandEmpty>
                <CommandGroup>
                  {availableLabels.map(label => (
                    <CommandItem
                      key={label.name}
                      onSelect={() => toggleLabel(label.name)}
                    >
                      <div className="flex items-center gap-2">
                        <div
                          className={`h-4 w-4 rounded border ${
                            filters.selectedLabels.includes(label.name)
                              ? 'bg-primary border-primary'
                              : 'border-input'
                          }`}
                        />
                        <div
                          className="h-3 w-3 rounded"
                          style={{ backgroundColor: label.color }}
                        />
                        <span>{label.name}</span>
                      </div>
                    </CommandItem>
                  ))}
                </CommandGroup>
              </CommandList>
            </Command>
          </PopoverContent>
        </Popover>

        {/* Subscriber Filter */}
        <Popover>
          <PopoverTrigger asChild>
            <Button variant="outline" className="gap-2">
              구독자
              {filters.selectedSubscribers.length > 0 && (
                <Badge variant="secondary" className="ml-1 px-1.5 py-0">
                  {filters.selectedSubscribers.length}
                </Badge>
              )}
            </Button>
          </PopoverTrigger>
          <PopoverContent className="w-[200px] p-0 bg-white" align="start">
            <Command>
              <CommandInput placeholder="구독자 검색..." />
              <CommandList>
                <CommandEmpty>결과 없음</CommandEmpty>
                <CommandGroup>
                  {availableSubscribers.map(subscriber => (
                    <CommandItem
                      key={subscriber.nickname}
                      onSelect={() => toggleSubscriber(subscriber.nickname)}
                    >
                      <div className="flex items-center gap-2">
                        <div
                          className={`h-4 w-4 rounded border ${
                            filters.selectedSubscribers.includes(
                              subscriber.nickname
                            )
                              ? 'bg-primary border-primary'
                              : 'border-input'
                          }`}
                        />
                        <span>{subscriber.nickname}</span>
                      </div>
                    </CommandItem>
                  ))}
                </CommandGroup>
              </CommandList>
            </Command>
          </PopoverContent>
        </Popover>

        {/* Clear All
        {hasActiveFilters && (
          <Button
            variant="ghost"
            size="sm"
            onClick={clearAllFilters}
            className="gap-1"
          >
            <X className="h-4 w-4" />
            초기화
          </Button>
        )} */}
      </div>

      {/* Active Filters Display */}
      {hasActiveFilters && (
        <div className="flex justify-between">
          <div className="flex flex-wrap gap-2">
            {filters.search && (
              <Badge variant="secondary" className="gap-1">
                검색: {filters.search}
                <X
                  className="h-3 w-3 cursor-pointer"
                  onClick={() => updateFilters({ search: '' })}
                />
              </Badge>
            )}
            {filters.selectedOwners.map(owner => (
              <Badge key={owner} variant="secondary" className="gap-1">
                담당: {owner}
                <X
                  className="h-3 w-3 cursor-pointer"
                  onClick={() => toggleOwner(owner)}
                />
              </Badge>
            ))}
            {filters.selectedLabels.map(label => {
              const labelObj = availableLabels.find(l => l.name === label)
              return (
                <Badge
                  key={label}
                  variant="outline"
                  className="gap-1"
                  style={{
                    backgroundColor: labelObj?.color,
                    borderColor: labelObj?.color,
                    color: '#fff',
                  }}
                >
                  {label}
                  <X
                    className="h-3 w-3 cursor-pointer"
                    onClick={() => toggleLabel(label)}
                  />
                </Badge>
              )
            })}
            {filters.selectedSubscribers.map(subscriber => (
              <Badge key={subscriber} variant="secondary" className="gap-1">
                구독: {subscriber}
                <X
                  className="h-3 w-3 cursor-pointer"
                  onClick={() => toggleSubscriber(subscriber)}
                />
              </Badge>
            ))}
          </div>
          {/* Clear All */}
          <Button
            variant="ghost"
            size="sm"
            onClick={clearAllFilters}
            className="gap-1"
          >
            <X className="h-4 w-4" />
            초기화
          </Button>
        </div>
      )}
    </div>
  )
}
