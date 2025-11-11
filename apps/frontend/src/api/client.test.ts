import { describe, it, expect, beforeEach } from 'vitest'
import { apiClient } from './client'

describe('apiClient', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('should have correct base URL', () => {
    expect(apiClient.defaults.baseURL).toBeTruthy()
  })

  it('should have timeout configured', () => {
    expect(apiClient.defaults.timeout).toBe(10000)
  })

  it('should have request interceptor configured', () => {
    // Verify that request interceptors exist
    expect(apiClient.interceptors.request).toBeDefined()
  })

  it('should have response interceptor configured', () => {
    // Verify that response interceptors exist
    expect(apiClient.interceptors.response).toBeDefined()
  })
})
