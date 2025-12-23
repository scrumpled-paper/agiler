import type { Issue } from '@/types/issue'
import type { KanbanFilters } from '@/components/kanban/KanbanFilterBar'

/**
 * Apply search filter to issues
 */
export function applySearchFilter(issues: Issue[], search: string): Issue[] {
  if (!search) return issues

  const searchLower = search.toLowerCase()
  return issues.filter(issue => issue.title.toLowerCase().includes(searchLower))
}

/**
 * Apply owner filter to issues
 */
export function applyOwnerFilter(
  issues: Issue[],
  selectedOwners: number[]
): Issue[] {
  if (selectedOwners.length === 0) return issues

  return issues.filter(issue =>
    issue.assignees.some(assignee => selectedOwners.includes(assignee))
  )
}

/**
 * Apply label filter to issues
 */
export function applyLabelFilter(
  issues: Issue[],
  selectedLabels: number[]
): Issue[] {
  if (selectedLabels.length === 0) return issues

  return issues.filter(issue =>
    issue.labels?.some(label => selectedLabels.includes(label))
  )
}

/**
 * Apply subscriber filter to issues
 */
export function applySubscriberFilter(
  issues: Issue[],
  selectedSubscribers: number[]
): Issue[] {
  if (selectedSubscribers.length === 0) return issues

  return issues.filter(issue =>
    issue.notis?.some(noti => selectedSubscribers.includes(noti))
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
        return new Date(a.dueAt).getTime() - new Date(b.dueAt).getTime()
      case 'endAt-desc':
        return new Date(b.dueAt).getTime() - new Date(a.dueAt).getTime()
      case 'startAt-asc':
        return new Date(a.startedAt).getTime() - new Date(b.startedAt).getTime()
      case 'startAt-desc':
        return new Date(b.startedAt).getTime() - new Date(a.startedAt).getTime()
      case 'name-asc':
        return a.title.localeCompare(b.title)
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
