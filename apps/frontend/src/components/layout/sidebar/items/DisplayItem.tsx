// components/layout/sidebar/items/DisplayItem.tsx

import { User } from 'lucide-react'
import type { ProjectMember } from '@/types'

interface DisplayItemProps {
  member: ProjectMember
}

export function DisplayItem({ member }: DisplayItemProps) {
  return (
    <div className="flex items-center gap-2 px-2 py-1.5">
      {member.imageUrl ? (
        <img
          src={member.imageUrl}
          alt={member.nickname}
          className="h-8 w-8 rounded-full object-cover"
        />
      ) : (
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-muted">
          <User className="h-4 w-4 text-muted-foreground" />
        </div>
      )}
      <div className="flex flex-col overflow-hidden">
        <span className="truncate text-sm font-medium">{member.nickname}</span>
        {member.role && (
          <span className="truncate text-xs text-muted-foreground">
            {member.role}
          </span>
        )}
      </div>
    </div>
  )
}
