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
    <div className="flex w-full flex-col gap-6">
      <div className="flex w-full flex-col gap-6">
        <h1 className="text-center text-[40px] font-bold leading-tight text-black">
          Current Members
        </h1>
      </div>

      <div className="flex w-full flex-col gap-7">
        <div className="flex w-full gap-2 px-8 py-5">
          <div className="flex-1 text-xl text-[#6d758f]">이름</div>
          <div className="w-[776px] text-xl text-[#6d758f]">권한</div>
        </div>

        <div className="flex w-full flex-col gap-7">
          {members.map((member, index) => (
            <MemberListItem
              key={index}
              member={member}
              onRoleChange={
                onRoleChange
                  ? newRole => onRoleChange(index, newRole)
                  : undefined
              }
              onRemove={onRemove ? () => onRemove(index) : undefined}
              canEdit={canEdit}
            />
          ))}
        </div>
      </div>
    </div>
  )
}
