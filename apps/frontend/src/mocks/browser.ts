import { setupWorker } from 'msw/browser'
import { handlers } from './handlers'

/**
 * MSW browser worker for development environment
 * This can be conditionally started in main.tsx using VITE_USE_MSW env variable
 */
export const worker = setupWorker(...handlers)
