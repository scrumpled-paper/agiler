// components/layout/sidebar/sections/UserInfoSection.tsx

import { User } from 'lucide-react'

interface UserInfoSectionProps {
  userName?: string
  userRole?: string
  userDescription?: string
  avatarUrl?: string
}

export function UserInfoSection({
  userName = 'John Doe',
  userRole = 'Project Manager',
  userDescription = 'Managing Projects with ease!',
  avatarUrl,
}: UserInfoSectionProps) {
  return (
    <div className="flex items-center gap-4 border-b bg-background p-6">
      {avatarUrl ? (
        <img
          src={avatarUrl}
          alt={userName}
          className="h-12 w-12 rounded-full object-cover"
        />
      ) : (
        <div className="flex h-12 w-12 items-center justify-center rounded-full bg-muted">
          <User className="h-6 w-6 text-muted-foreground" />
        </div>
      )}
      <div className="flex flex-1 flex-col gap-1">
        <p className="text-sm font-bold">{userName}</p>
        {userRole && (
          <div className="inline-flex items-center rounded-sm border bg-muted px-1.5 py-0.5">
            <span className="text-xs text-muted-foreground">{userRole}</span>
          </div>
        )}
        {userDescription && (
          <p className="text-xs text-muted-foreground">{userDescription}</p>
        )}
      </div>
    </div>
  )
}
