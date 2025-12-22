import { X } from 'lucide-react'
import { Avatar } from '@/components/ui/avatar'
import type { ProjectMember } from '@/types'

type SelectedAssigneesProps = {
  assignees: ProjectMember[]
  onRemove: (peopleId: number) => void
}

export function SelectedAssignees({
  assignees,
  onRemove,
}: SelectedAssigneesProps) {
  if (assignees.length === 0) return null

  return (
    <div className="flex flex-wrap items-center gap-2">
      {assignees.map(member => (
        <div
          key={member.peopleId}
          className="flex h-[43px] items-center gap-2 rounded-[5px] border-[0.67px] border-[#e1e4ed] bg-white px-3 py-3 shadow-sm"
        >
          <Avatar className="h-8 w-8">
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
          <span className="text-[11px] font-semibold text-[#6d758f]">
            {member.nickname}
          </span>
          <button
            type="button"
            onClick={() => onRemove(member.peopleId)}
            className="ml-1 text-[#6d758f] hover:text-black"
          >
            <X className="h-3 w-3" />
          </button>
        </div>
      ))}
    </div>
  )
}
