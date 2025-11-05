interface SidebarMemberItemProps {
  nickname: string
  imageUrl: string
  role: string
}

export const SidebarMemberItem = ({
  nickname,
  imageUrl,
  role,
}: SidebarMemberItemProps) => {
  return (
    <div className="flex items-center gap-2 px-2 py-1.5">
      <img src={imageUrl} className="h-4 w-4" />
      <div className="flex flex-col">
        <span className="text-sm font-medium">{nickname}</span>
        {role && <span className="text-xs text-muted-foreground">{role}</span>}
      </div>
    </div>
  )
}
