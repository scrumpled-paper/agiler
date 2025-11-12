// vitest의 expect 함수에 jest-dom의 matcher들을 추가합니다.
// 예: expect(element).toBeInTheDocument();
import '@testing-library/jest-dom'
import { server } from './mocks/server'
import { beforeAll, afterEach, afterAll } from 'vitest'

// MSW 서버 설정
beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())
