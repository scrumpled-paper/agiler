// components/layout/sidebar/sections/UserInfoSection.tsx

import { useUserInfo } from '@/lib/sidebar/hooks'
import { User } from 'lucide-react'

export function UserInfoSection() {
  const { data: user, isLoading, error } = useUserInfo()
  if (isLoading) {
    return <div>Loading user info...</div>
  }

  if (error || !user) {
    return <div>Failed to load user info.</div>
  }
  return (
    <div className="flex items-center gap-4 border-b bg-background p-6">
      {user.image ? (
        <img
          src={user.image}
          alt={user.nickname}
          className="h-12 w-12 rounded-full object-cover"
        />
      ) : (
        <div className="flex h-12 w-12 items-center justify-center rounded-full bg-muted">
          <User className="h-6 w-6 text-muted-foreground" />
        </div>
      )}
      <div className="flex flex-1 flex-col gap-1">
        <p className="text-sm font-bold">{user.nickname}</p>
        {user.email ? (
          <p className="text-xs text-muted-foreground">{user.email}</p>
        ) : (
          <p className="text-xs text-muted-foreground">
            등록된 이메일이 없습니다.
          </p>
        )}
      </div>
    </div>
  )
}
