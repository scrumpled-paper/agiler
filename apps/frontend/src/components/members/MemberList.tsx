import { MemberListItem, type MemberListItemProps } from './MemberListItem'

export interface MemberListProps {
  members: MemberListItemProps['member'][]
  onRoleChange?: (memberId: number, newRole: string) => void
  onRemove?: (memberId: number) => void
  canEdit?: boolean
}

export function MemberList({
  members,
  onRoleChange,
  onRemove,
  canEdit = true,
}: MemberListProps) {
  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-6">
        <h1 className="text-center text-[40px] font-bold leading-tight text-black">
          Current Members
        </h1>
      </div>

      <div className="flex flex-col gap-7">
        {members.map((member, index) => (
          <MemberListItem
            key={index}
            member={member}
            onRoleChange={
              onRoleChange ? newRole => onRoleChange(index, newRole) : undefined
            }
            onRemove={onRemove ? () => onRemove(index) : undefined}
            canEdit={canEdit}
          />
        ))}
      </div>
    </div>
  )
}
