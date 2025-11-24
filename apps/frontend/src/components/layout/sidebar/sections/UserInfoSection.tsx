// components/layout/sidebar/sections/UserInfoSection.tsx

import { User } from 'lucide-react'
import type { UserInfo } from '@/types'

interface UserInfoSectionProps {
  userInfo?: UserInfo
  context: string
}

export function UserInfoSection({ userInfo, context }: UserInfoSectionProps) {
  if (!userInfo) {
    return <div>Loading user info...</div>
  }
  const userEmail = userInfo.email || '등록된 이메일이 없습니다.'
  return (
    <div className="flex items-center gap-4 border-b bg-background p-6">
      {userInfo.imageUrl ? (
        <img
          src={userInfo.imageUrl}
          alt={userInfo.nickname}
          className="h-12 w-12 rounded-full object-cover"
        />
      ) : (
        <div className="flex h-12 w-12 items-center justify-center rounded-full bg-muted">
          <User className="h-6 w-6 text-muted-foreground" />
        </div>
      )}
      <div className="flex flex-1 flex-col gap-1">
        <p className="text-sm font-bold">{userInfo.nickname}</p>

        {context === 'dashboard' ? (
          <p className="text-xs text-muted-foreground">{userEmail}</p>
        ) : (
          <p className="text-xs text-muted-foreground">{userInfo.role}</p>
        )}
      </div>
    </div>
  )
}
