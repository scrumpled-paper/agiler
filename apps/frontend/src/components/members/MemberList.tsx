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
    <div className="flex flex-col justify-center items-center w-full">
      <div className="flex w-full flex-col gap-5 max-w-3xl ">
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
