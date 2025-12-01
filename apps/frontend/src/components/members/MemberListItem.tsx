import { X } from 'lucide-react'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'

export interface MemberListItemProps {
  member: {
    nickname: string
    imageUrl?: string
    role: string
  }
  onRoleChange?: (newRole: string) => void
  onRemove?: () => void
  canEdit?: boolean
}

export function MemberListItem({
  member,
  onRoleChange,
  onRemove,
  canEdit = true,
}: MemberListItemProps) {
  const getInitials = (name: string) => {
    return name
      .split(' ')
      .map(n => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2)
  }

  return (
    <div className="relative flex items-center justify-start gap-10 rounded-lg border border-[#e1e4ed] bg-white px-6 py-6">
      <div className="flex items-center gap-4">
        <Avatar className="size-8">
          <AvatarImage src={member.imageUrl} alt={member.nickname} />
          <AvatarFallback className="bg-[rgba(0,0,0,0.1)] text-sm">
            {getInitials(member.nickname)}
          </AvatarFallback>
        </Avatar>
        <span className="text-base font-medium text-black">
          {member.nickname}
        </span>
      </div>

      <div className="flex items-center gap-10 px-10">
        <Select
          value={member.role}
          onValueChange={onRoleChange}
          disabled={!canEdit}
        >
          <SelectTrigger className="h-auto w-[180px] rounded-md border-[#e1e4ed] bg-[#f8faff] px-5 py-3 text-sm font-semibold text-[#6d758f]">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="OWNER">owner</SelectItem>
            <SelectItem value="MEMBER">member</SelectItem>
          </SelectContent>
        </Select>

        {canEdit && onRemove && (
          <button
            onClick={onRemove}
            className="text-[#6d758f] transition-colors hover:text-black absolute end-10"
            aria-label="Remove member"
          >
            <X className="size-5" />
          </button>
        )}
      </div>
    </div>
  )
}
