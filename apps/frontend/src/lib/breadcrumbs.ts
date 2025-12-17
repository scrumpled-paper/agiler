// src/lib/breadcrumbs.ts (새 파일)
export interface BreadcrumbItem {
  label: string
  href?: string
}

export function getBreadcrumbs(
  pathname: string,
  params: { projectUrl?: string }
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
  if (pathname.startsWith('/projects/') && params.projectUrl) {
    breadcrumbs.push({
      label: `${params.projectUrl}`,
      href: `/projects/${params.projectUrl}`,
    })

    if (pathname.includes('/settings')) {
      breadcrumbs.push({ label: 'Settings' })
    } else if (pathname.includes('/dailyscrums')) {
      breadcrumbs.push({
        label: 'Daily Scrum',
        href: `/projects/${params.projectUrl}/dailyscrums`,
      })
    } else if (pathname.includes('/retrospectives')) {
      breadcrumbs.push({
        label: 'Retrospectives',
        href: `/projects/${params.projectUrl}/retrospectives`,
      })
    } else if (pathname.includes('/meetings')) {
      breadcrumbs.push({
        label: 'Meetings',
        href: `/projects/${params.projectUrl}/meetings`,
      })
    }
    return breadcrumbs
  }

  return breadcrumbs
}
