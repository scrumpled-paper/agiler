import { create } from 'zustand'

interface NotificationSheetStore {
  isOpen: boolean
  open: () => void
  close: () => void
  toggle: () => void
}

export const useNotificationSheet = create<NotificationSheetStore>(set => ({
  isOpen: false,
  open: () => set({ isOpen: true }),
  close: () => set({ isOpen: false }),
  toggle: () => set(state => ({ isOpen: !state.isOpen })),
}))
