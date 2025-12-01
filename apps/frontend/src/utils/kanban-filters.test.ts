import { describe, it, expect } from 'vitest'
import type { Issue } from '@/types'
import {
  applySearchFilter,
  applyOwnerFilter,
  applyLabelFilter,
  applySubscriberFilter,
  sortIssues,
  filterAndSortIssues,
} from './kanban-filters'
import type { KanbanFilters } from '@/components/kanban/KanbanFilterBar'

describe('kanban-filters', () => {
  const mockIssues: Issue[] = [
    {
      id: '1',
      name: 'Fix login bug',
      column: 'todo',
      owner: {
        nickname: 'alice',
        email: 'alice@example.com',
        imageUrl: 'https://placehold.co/100x100',
      },
      startAt: new Date('2025-01-01'),
      endAt: new Date('2025-01-10'),
      labels: [
        {
          name: 'bug',
          description: 'Server-side logic and API tasks',
          color: '#ff0000',
        },
        {
          name: 'high-priority',
          description: 'Server-side logic and API tasks',
          color: '#ff9900',
        },
      ],
      subscribers: [
        {
          nickname: 'bob',
          email: 'bob@example.com',
          imageUrl: 'https://placehold.co/100x100',
        },
      ],
    },
    {
      id: '2',
      name: 'Add new feature',
      column: 'in-progress',
      owner: {
        nickname: 'bob',
        email: 'bob@example.com',
        imageUrl: 'https://placehold.co/100x100',
      },
      startAt: new Date('2025-01-05'),
      endAt: new Date('2025-01-20'),
      labels: [
        {
          name: 'feature',
          description: 'Server-side logic and API tasks',
          color: '#00ff00',
        },
      ],
      subscribers: [
        {
          nickname: 'alice',
          email: 'alice@example.com',
          imageUrl: 'https://placehold.co/100x100',
        },
        {
          nickname: 'charlie',
          email: 'charlie@example.com',
          imageUrl: 'https://placehold.co/100x100',
        },
      ],
    },
    {
      id: '3',
      name: 'Update documentation',
      column: 'done',
      owner: {
        nickname: 'charlie',
        email: 'charlie@example.com',
        imageUrl: 'https://placehold.co/100x100',
      },
      startAt: new Date('2025-01-03'),
      endAt: new Date('2025-01-08'),
      labels: [
        {
          name: 'documentation',
          description: 'Server-side logic and API tasks',
          color: '#0000ff',
        },
      ],
      subscribers: [],
    },
  ]

  describe('applySearchFilter', () => {
    it('should return all issues when search is empty', () => {
      const result = applySearchFilter(mockIssues, '')
      expect(result).toEqual(mockIssues)
    })

    it('should filter issues by name (case-insensitive)', () => {
      const result = applySearchFilter(mockIssues, 'bug')
      expect(result).toHaveLength(1)
      expect(result[0].name).toBe('Fix login bug')
    })

    it('should filter issues by partial name match', () => {
      const result = applySearchFilter(mockIssues, 'feature')
      expect(result).toHaveLength(1)
      expect(result[0].name).toBe('Add new feature')
    })

    it('should return empty array when no matches found', () => {
      const result = applySearchFilter(mockIssues, 'nonexistent')
      expect(result).toHaveLength(0)
    })

    it('should be case-insensitive', () => {
      const result = applySearchFilter(mockIssues, 'FIX')
      expect(result).toHaveLength(1)
      expect(result[0].name).toBe('Fix login bug')
    })
  })

  describe('applyOwnerFilter', () => {
    it('should return all issues when no owners selected', () => {
      const result = applyOwnerFilter(mockIssues, [])
      expect(result).toEqual(mockIssues)
    })

    it('should filter issues by single owner', () => {
      const result = applyOwnerFilter(mockIssues, ['alice'])
      expect(result).toHaveLength(1)
      expect(result[0].owner.nickname).toBe('alice')
    })

    it('should filter issues by multiple owners', () => {
      const result = applyOwnerFilter(mockIssues, ['alice', 'bob'])
      expect(result).toHaveLength(2)
      expect(result.map(i => i.owner.nickname)).toEqual(['alice', 'bob'])
    })

    it('should return empty array when owner not found', () => {
      const result = applyOwnerFilter(mockIssues, ['nonexistent'])
      expect(result).toHaveLength(0)
    })
  })

  describe('applyLabelFilter', () => {
    it('should return all issues when no labels selected', () => {
      const result = applyLabelFilter(mockIssues, [])
      expect(result).toEqual(mockIssues)
    })

    it('should filter issues by single label', () => {
      const result = applyLabelFilter(mockIssues, ['bug'])
      expect(result).toHaveLength(1)
      expect(result[0].labels?.some(l => l.name === 'bug')).toBe(true)
    })

    it('should filter issues by multiple labels (OR logic)', () => {
      const result = applyLabelFilter(mockIssues, ['bug', 'feature'])
      expect(result).toHaveLength(2)
    })

    it('should handle issues with multiple labels', () => {
      const result = applyLabelFilter(mockIssues, ['high-priority'])
      expect(result).toHaveLength(1)
      expect(result[0].id).toBe('1')
    })

    it('should return empty array when label not found', () => {
      const result = applyLabelFilter(mockIssues, ['nonexistent'])
      expect(result).toHaveLength(0)
    })

    it('should handle issues with no labels', () => {
      const issuesWithoutLabels: Issue[] = [
        {
          ...mockIssues[0],
          labels: undefined,
        },
      ]
      const result = applyLabelFilter(issuesWithoutLabels, ['bug'])
      expect(result).toHaveLength(0)
    })
  })

  describe('applySubscriberFilter', () => {
    it('should return all issues when no subscribers selected', () => {
      const result = applySubscriberFilter(mockIssues, [])
      expect(result).toEqual(mockIssues)
    })

    it('should filter issues by single subscriber', () => {
      const result = applySubscriberFilter(mockIssues, ['alice'])
      expect(result).toHaveLength(1)
      expect(result[0].id).toBe('2')
    })

    it('should filter issues by multiple subscribers (OR logic)', () => {
      const result = applySubscriberFilter(mockIssues, ['alice', 'bob'])
      expect(result).toHaveLength(2)
    })

    it('should return empty array when subscriber not found', () => {
      const result = applySubscriberFilter(mockIssues, ['nonexistent'])
      expect(result).toHaveLength(0)
    })

    it('should handle issues with no subscribers', () => {
      const result = applySubscriberFilter(mockIssues, ['alice'])
      expect(result.some(i => i.id === '3')).toBe(false)
    })

    it('should handle issues with multiple subscribers', () => {
      const result = applySubscriberFilter(mockIssues, ['charlie'])
      expect(result).toHaveLength(1)
      expect(result[0].id).toBe('2')
    })
  })

  describe('sortIssues', () => {
    it('should sort by endAt ascending', () => {
      const result = sortIssues(mockIssues, 'endAt-asc')
      expect(result[0].id).toBe('3') // 2025-01-08
      expect(result[1].id).toBe('1') // 2025-01-10
      expect(result[2].id).toBe('2') // 2025-01-20
    })

    it('should sort by endAt descending', () => {
      const result = sortIssues(mockIssues, 'endAt-desc')
      expect(result[0].id).toBe('2') // 2025-01-20
      expect(result[1].id).toBe('1') // 2025-01-10
      expect(result[2].id).toBe('3') // 2025-01-08
    })

    it('should sort by startAt ascending', () => {
      const result = sortIssues(mockIssues, 'startAt-asc')
      expect(result[0].id).toBe('1') // 2025-01-01
      expect(result[1].id).toBe('3') // 2025-01-03
      expect(result[2].id).toBe('2') // 2025-01-05
    })

    it('should sort by startAt descending', () => {
      const result = sortIssues(mockIssues, 'startAt-desc')
      expect(result[0].id).toBe('2') // 2025-01-05
      expect(result[1].id).toBe('3') // 2025-01-03
      expect(result[2].id).toBe('1') // 2025-01-01
    })

    it('should sort by name ascending (alphabetically)', () => {
      const result = sortIssues(mockIssues, 'name-asc')
      expect(result[0].name).toBe('Add new feature')
      expect(result[1].name).toBe('Fix login bug')
      expect(result[2].name).toBe('Update documentation')
    })

    it('should not modify original array', () => {
      const original = [...mockIssues]
      sortIssues(mockIssues, 'name-asc')
      expect(mockIssues).toEqual(original)
    })
  })

  describe('filterAndSortIssues', () => {
    it('should apply all filters and sorting', () => {
      const filters: KanbanFilters = {
        search: 'feature',
        sortBy: 'endAt-asc',
        selectedOwners: ['bob'],
        selectedLabels: ['feature'],
        selectedSubscribers: [],
      }

      const result = filterAndSortIssues(mockIssues, filters)
      expect(result).toHaveLength(1)
      expect(result[0].id).toBe('2')
    })

    it('should handle empty filters', () => {
      const filters: KanbanFilters = {
        search: '',
        sortBy: 'endAt-asc',
        selectedOwners: [],
        selectedLabels: [],
        selectedSubscribers: [],
      }

      const result = filterAndSortIssues(mockIssues, filters)
      expect(result).toHaveLength(3)
      expect(result[0].id).toBe('3') // sorted by endAt asc
    })

    it('should return empty array when filters match nothing', () => {
      const filters: KanbanFilters = {
        search: 'nonexistent',
        sortBy: 'endAt-asc',
        selectedOwners: [],
        selectedLabels: [],
        selectedSubscribers: [],
      }

      const result = filterAndSortIssues(mockIssues, filters)
      expect(result).toHaveLength(0)
    })

    it('should combine multiple filters correctly', () => {
      const filters: KanbanFilters = {
        search: '',
        sortBy: 'name-asc',
        selectedOwners: [],
        selectedLabels: ['bug', 'feature'],
        selectedSubscribers: [],
      }

      const result = filterAndSortIssues(mockIssues, filters)
      expect(result).toHaveLength(2)
      expect(result[0].name).toBe('Add new feature') // sorted alphabetically
      expect(result[1].name).toBe('Fix login bug')
    })

    it('should not modify original array', () => {
      const original = [...mockIssues]
      const filters: KanbanFilters = {
        search: 'bug',
        sortBy: 'name-asc',
        selectedOwners: [],
        selectedLabels: [],
        selectedSubscribers: [],
      }

      filterAndSortIssues(mockIssues, filters)
      expect(mockIssues).toEqual(original)
    })

    it('should handle subscriber filter with other filters', () => {
      const filters: KanbanFilters = {
        search: '',
        sortBy: 'endAt-asc',
        selectedOwners: [],
        selectedLabels: [],
        selectedSubscribers: ['alice', 'bob'],
      }

      const result = filterAndSortIssues(mockIssues, filters)
      expect(result).toHaveLength(2)
      expect(result.map(i => i.id)).toEqual(['1', '2']) // sorted by endAt
    })
  })
})
