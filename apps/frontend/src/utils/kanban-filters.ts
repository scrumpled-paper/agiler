import type { Issue } from '@/types/issue'
import type { KanbanFilters } from '@/components/kanban/KanbanFilterBar'

/**
 * Apply search filter to issues
 */
export function applySearchFilter(issues: Issue[], search: string): Issue[] {
  if (!search) return issues

  const searchLower = search.toLowerCase()
  return issues.filter(issue => issue.name.toLowerCase().includes(searchLower))
}

/**
 * Apply owner filter to issues
 */
export function applyOwnerFilter(
  issues: Issue[],
  selectedOwners: string[]
): Issue[] {
  if (selectedOwners.length === 0) return issues

  return issues.filter(issue => selectedOwners.includes(issue.owner.nickname))
}

/**
 * Apply label filter to issues
 */
export function applyLabelFilter(
  issues: Issue[],
  selectedLabels: string[]
): Issue[] {
  if (selectedLabels.length === 0) return issues

  return issues.filter(issue =>
    issue.labels?.some(label => selectedLabels.includes(label.name))
  )
}

/**
 * Apply subscriber filter to issues
 */
export function applySubscriberFilter(
  issues: Issue[],
  selectedSubscribers: string[]
): Issue[] {
  if (selectedSubscribers.length === 0) return issues

  return issues.filter(issue =>
    issue.subscribers?.some(subscriber =>
      selectedSubscribers.includes(subscriber.nickname)
    )
  )
}

/**
 * Sort issues based on the sort option
 */
export function sortIssues(
  issues: Issue[],
  sortBy: KanbanFilters['sortBy']
): Issue[] {
  const sorted = [...issues]

  sorted.sort((a, b) => {
    switch (sortBy) {
      case 'endAt-asc':
        return new Date(a.endAt).getTime() - new Date(b.endAt).getTime()
      case 'endAt-desc':
        return new Date(b.endAt).getTime() - new Date(a.endAt).getTime()
      case 'startAt-asc':
        return new Date(a.startAt).getTime() - new Date(b.startAt).getTime()
      case 'startAt-desc':
        return new Date(b.startAt).getTime() - new Date(a.startAt).getTime()
      case 'name-asc':
        return a.name.localeCompare(b.name)
      default:
        return 0
    }
  })

  return sorted
}

/**
 * Apply all filters and sorting to issues
 */
export function filterAndSortIssues(
  issues: Issue[],
  filters: KanbanFilters
): Issue[] {
  let result = [...issues]

  // Apply filters
  result = applySearchFilter(result, filters.search)
  result = applyOwnerFilter(result, filters.selectedOwners)
  result = applyLabelFilter(result, filters.selectedLabels)
  result = applySubscriberFilter(result, filters.selectedSubscribers)

  // Apply sorting
  result = sortIssues(result, filters.sortBy)

  return result
}
