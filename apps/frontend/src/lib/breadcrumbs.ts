// src/lib/breadcrumbs.ts (새 파일)
export interface BreadcrumbItem {
  label: string
  href?: string
}

export function getBreadcrumbs(
  pathname: string,
  params: { projectId?: string; scrumId?: string }
): BreadcrumbItem[] {
  const breadcrumbs: BreadcrumbItem[] = [
    { label: 'Dashboard', href: '/dashboard' },
  ]

  // Dashboard 경로
  if (pathname.startsWith('/dashboard')) {
    if (pathname.includes('/settings')) {
      breadcrumbs.push({ label: 'Settings' })
    }
    return breadcrumbs
  }

  // Projects 경로
  if (pathname.startsWith('/projects/') && params.projectId) {
    breadcrumbs.push({
      label: `Project ${params.projectId}`,
      href: `/projects/${params.projectId}`,
    })

    if (pathname.includes('/settings')) {
      breadcrumbs.push({ label: 'Settings' })
    } else if (pathname.includes('/daily-scrum')) {
      breadcrumbs.push({
        label: 'Daily Scrum',
        href: `/projects/${params.projectId}/daily-scrum`,
      })

      if (params.scrumId) {
        breadcrumbs.push({ label: `Scrum #${params.scrumId}` })
      }
    }
    return breadcrumbs
  }

  return breadcrumbs
}
