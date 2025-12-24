import { Button } from '@/components/ui/button'
import { Avatar } from '@/components/ui/avatar'
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
import type { UserInfo } from '@/types'
import { Plus } from 'lucide-react'

type AssigneeSelectorProps = {
  members: UserInfo[]
  onAdd: (member: UserInfo) => void
  isOpen: boolean
  onOpenChange: (open: boolean) => void
}

export function AssigneeSelector({
  members,
  onAdd,
  isOpen,
  onOpenChange,
}: AssigneeSelectorProps) {
  return (
    <Popover open={isOpen} onOpenChange={onOpenChange}>
      <PopoverTrigger asChild>
        {/* <div className="flex w-4xs h-[38px] items-center justify-between rounded-md border border-[#f1f3f7] bg-white px-3 py-1.5 shadow-sm"> */}
        <div className="flex w-4xs h-[38px] items-center justify-between ">
          {/* <span className="text-xs text-[#6d758f]">Select assignee</span> */}
          <Button
            type="button"
            size="sm"
            // className="h-8 bg-[#6d758f] px-3 text-xs hover:bg-[#6d758f]/90"
            className="h-8 bg-[#6d758f] px-3 text-xs hover:bg-[#6d758f]/90"
          >
            <Plus />
          </Button>
        </div>
      </PopoverTrigger>
      <PopoverContent className="p-0 bg-white" align="start">
        <Command>
          <CommandInput placeholder="Search member..." />
          <CommandList>
            <CommandEmpty>No member found.</CommandEmpty>
            <CommandGroup>
              {members.map(member => (
                <CommandItem
                  key={member.profileId}
                  onSelect={() => onAdd(member)}
                  className="flex items-center gap-2"
                >
                  <Avatar className="h-6 w-6">
                    {member.imageUrl ? (
                      <img
                        src={member.imageUrl}
                        alt={member.nickname}
                        className="h-full w-full object-cover"
                      />
                    ) : (
                      <div className="flex h-full w-full items-center justify-center bg-[#f1f3f7] text-xs text-[#6d758f]">
                        {member.nickname[0]}
                      </div>
                    )}
                  </Avatar>
                  <span className="text-sm">{member.nickname}</span>
                </CommandItem>
              ))}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  )
}
