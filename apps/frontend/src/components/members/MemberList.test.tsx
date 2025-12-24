import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemberList } from './MemberList'
import type { UserInfo } from '@/types'

const mockMembers: UserInfo[] = [
  {
    profileId: 1,
    nickname: 'Alice Johnson',
    email: 'alice@example.com',
    imageUrl: 'https://placehold.co/100x100',
    role: 'OWNER',
  },
  {
    profileId: 2,
    nickname: 'Bob Smith',
    email: 'bob@example.com',
    imageUrl: 'https://placehold.co/100x100',
    role: 'MEMBER',
  },
  {
    profileId: 3,
    nickname: 'Charlie Lee',
    email: 'charlie@example.com',
    imageUrl: '',
    role: 'MEMBER',
  },
]

describe('MemberList - 통합 테스트', () => {
  describe('멤버 목록 렌더링', () => {
    it('전달받은 모든 멤버를 렌더링한다', () => {
      render(<MemberList members={mockMembers} />)

      expect(screen.getByText('Alice Johnson')).toBeInTheDocument()
      expect(screen.getByText('Bob Smith')).toBeInTheDocument()
      expect(screen.getByText('Charlie Lee')).toBeInTheDocument()
    })

    it('멤버가 없을 때 빈 목록을 렌더링한다', () => {
      render(<MemberList members={[]} />)

      // 멤버 이름이 표시되지 않는지 확인
      expect(screen.queryByText('Alice Johnson')).not.toBeInTheDocument()
      expect(screen.queryByText('Bob Smith')).not.toBeInTheDocument()
    })

    it('각 멤버의 역할을 표시한다', () => {
      render(<MemberList members={mockMembers} />)

      // owner와 member 역할이 표시되는지 확인
      const ownerElements = screen.getAllByText(/owner/i)
      const memberElements = screen.getAllByText(/member/i)

      expect(ownerElements.length).toBeGreaterThan(0)
      expect(memberElements.length).toBeGreaterThan(0)
    })
  })

  describe('멤버 아바타', () => {
    it('멤버의 이니셜을 표시한다', () => {
      render(<MemberList members={mockMembers} />)

      // Alice Johnson의 이니셜 'AJ'가 표시되는지 확인
      expect(screen.getByText('AJ')).toBeInTheDocument()
      // Bob Smith의 이니셜 'BS'가 표시되는지 확인
      expect(screen.getByText('BS')).toBeInTheDocument()
      // Charlie Lee의 이니셜 'CL'이 표시되는지 확인
      expect(screen.getByText('CL')).toBeInTheDocument()
    })
  })

  describe('역할 변경 기능', () => {
    it('canEdit이 true일 때 역할 선택 드롭다운이 활성화된다', () => {
      render(<MemberList members={mockMembers} canEdit={true} />)

      // Select 컴포넌트들이 disabled가 아닌지 확인
      const selectTriggers = screen.getAllByRole('combobox')
      selectTriggers.forEach(trigger => {
        expect(trigger).not.toBeDisabled()
      })
    })

    it('canEdit이 false일 때 역할 선택 드롭다운이 비활성화된다', () => {
      render(<MemberList members={mockMembers} canEdit={false} />)

      // Select 컴포넌트들이 disabled인지 확인
      const selectTriggers = screen.getAllByRole('combobox')
      selectTriggers.forEach(trigger => {
        expect(trigger).toBeDisabled()
      })
    })

    it('역할 변경 시 onRoleChange 콜백이 호출된다', async () => {
      const user = userEvent.setup()
      const onRoleChange = vi.fn()

      render(
        <MemberList
          members={mockMembers}
          onRoleChange={onRoleChange}
          canEdit={true}
        />
      )

      // 첫 번째 멤버의 역할 선택 드롭다운 클릭
      const selectTriggers = screen.getAllByRole('combobox')
      await user.click(selectTriggers[0])

      // 'member' 옵션 선택
      const memberOption = screen.getByRole('option', { name: /member/i })
      await user.click(memberOption)

      // onRoleChange가 올바른 인자로 호출되었는지 확인
      expect(onRoleChange).toHaveBeenCalledWith(0, 'MEMBER')
    })
  })

  describe('멤버 제거 기능', () => {
    it('canEdit이 true이고 onRemove가 제공되면 제거 버튼이 표시된다', () => {
      const onRemove = vi.fn()

      render(
        <MemberList members={mockMembers} canEdit={true} onRemove={onRemove} />
      )

      // 제거 버튼들이 표시되는지 확인 (aria-label 사용)
      const removeButtons = screen.getAllByLabelText('Remove member')
      expect(removeButtons).toHaveLength(mockMembers.length)
    })

    it('canEdit이 false일 때 제거 버튼이 표시되지 않는다', () => {
      const onRemove = vi.fn()

      render(
        <MemberList members={mockMembers} canEdit={false} onRemove={onRemove} />
      )

      // 제거 버튼이 없어야 함
      const removeButtons = screen.queryAllByLabelText('Remove member')
      expect(removeButtons).toHaveLength(0)
    })

    it('제거 버튼 클릭 시 onRemove 콜백이 호출된다', async () => {
      const user = userEvent.setup()
      const onRemove = vi.fn()

      render(
        <MemberList members={mockMembers} canEdit={true} onRemove={onRemove} />
      )

      // 첫 번째 멤버의 제거 버튼 클릭
      const removeButtons = screen.getAllByLabelText('Remove member')
      await user.click(removeButtons[0])

      // onRemove가 첫 번째 멤버의 인덱스로 호출되었는지 확인
      expect(onRemove).toHaveBeenCalledWith(0)
    })
  })

  describe('스타일링', () => {
    it('컨테이너가 올바른 레이아웃 클래스를 가진다', () => {
      const { container } = render(<MemberList members={mockMembers} />)

      const wrapper = container.firstChild as HTMLElement
      expect(wrapper).toHaveClass(
        'flex',
        'flex-col',
        'justify-center',
        'items-center',
        'w-full'
      )
    })
  })
})
