import { describe, it, expect } from 'vitest'
import type { Issue } from '@/types/issue'
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
  // 변경된 Interface에 맞춘 Mock Data (ID 기반)
  const mockIssues: Issue[] = [
    {
      id: '1',
      name: 'Fix login bug',
      column: '1',
      issueId: '1',
      title: 'Fix login bug',
      kanbanConfigId: 1,
      assignees: [101], // Alice ID
      startedAt: '2025-01-01T00:00:00.000Z',
      dueAt: '2025-01-10T00:00:00.000Z',
      createdAt: '2025-01-01T00:00:00.000Z',
      isDone: false,
      labels: [201, 202], // bug, high-priority ID
      notis: [102], // Bob ID
    },
    {
      id: '2',
      name: 'Add new feature',
      column: '2',
      issueId: '2',
      title: 'Add new feature',
      kanbanConfigId: 2,
      assignees: [102], // Bob ID
      startedAt: '2025-01-05T00:00:00.000Z',
      dueAt: '2025-01-20T00:00:00.000Z',
      createdAt: '2025-01-05T00:00:00.000Z',
      isDone: false,
      labels: [203], // feature ID
      notis: [101, 103], // Alice, Charlie ID
    },
    {
      id: '3',
      name: 'Update documentation',
      column: '3',
      issueId: '3',
      title: 'Update documentation',
      kanbanConfigId: 3,
      assignees: [103], // Charlie ID
      startedAt: '2025-01-03T00:00:00.000Z',
      dueAt: '2025-01-08T00:00:00.000Z',
      createdAt: '2025-01-03T00:00:00.000Z',
      isDone: false,
      labels: [204], // documentation ID
      notis: [],
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
      expect(result[0].title).toBe('Fix login bug')
    })
  })

  describe('applyOwnerFilter', () => {
    it('should return all issues when no owners selected', () => {
      const result = applyOwnerFilter(mockIssues, [])
      expect(result).toEqual(mockIssues)
    })

    it('should filter issues by single owner ID', () => {
      const result = applyOwnerFilter(mockIssues, [101])
      expect(result).toHaveLength(1)
      expect(result[0].assignees).toContain(101)
    })

    it('should filter issues by multiple owner IDs', () => {
      const result = applyOwnerFilter(mockIssues, [101, 102])
      expect(result).toHaveLength(2)
      expect(result.map(i => i.assignees[0])).toEqual([101, 102])
    })
  })

  describe('applyLabelFilter', () => {
    it('should return all issues when no labels selected', () => {
      const result = applyLabelFilter(mockIssues, [])
      expect(result).toEqual(mockIssues)
    })

    it('should filter issues by single label ID', () => {
      const result = applyLabelFilter(mockIssues, [201])
      expect(result).toHaveLength(1)
      expect(result[0].labels).toContain(201)
    })

    it('should filter issues by multiple label IDs (OR logic)', () => {
      const result = applyLabelFilter(mockIssues, [201, 203])
      expect(result).toHaveLength(2)
    })
  })

  describe('applySubscriberFilter', () => {
    it('should return all issues when no subscribers selected', () => {
      const result = applySubscriberFilter(mockIssues, [])
      expect(result).toEqual(mockIssues)
    })

    it('should filter issues by single subscriber ID', () => {
      const result = applySubscriberFilter(mockIssues, [101])
      expect(result).toHaveLength(1)
      expect(result[0].issueId).toBe('2')
    })

    it('should handle issues with no subscribers', () => {
      const result = applySubscriberFilter(mockIssues, [101])
      expect(result.some(i => i.issueId === '3')).toBe(false)
    })
  })

  describe('sortIssues', () => {
    it('should sort by endAt ascending', () => {
      const result = sortIssues(mockIssues, 'endAt-asc')
      expect(result[0].issueId).toBe('3') // 2025-01-08
      expect(result[1].issueId).toBe('1') // 2025-01-10
      expect(result[2].issueId).toBe('2') // 2025-01-20
    })
  })

  describe('filterAndSortIssues', () => {
    it('should apply all filters (using IDs) and sorting', () => {
      const filters: KanbanFilters = {
        search: 'feature',
        sortBy: 'endAt-asc',
        selectedOwners: [102], // Bob ID
        selectedLabels: [203], // feature ID
        selectedSubscribers: [],
      }

      const result = filterAndSortIssues(mockIssues, filters)
      expect(result).toHaveLength(1)
      expect(result[0].issueId).toBe('2')
    })
  })
})
