// components/layout/sidebar/buttons/ShowMoreButton.tsx

import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/button'

interface ShowMoreButtonProps {
  to: string
  label?: string
}

export function ShowMoreButton({ to, label = '더보기' }: ShowMoreButtonProps) {
  return (
    <Button
      variant="ghost"
      size="sm"
      asChild
      className="w-full justify-center text-xs text-muted-foreground hover:text-foreground"
    >
      <Link to={to}>{label}</Link>
    </Button>
  )
}
