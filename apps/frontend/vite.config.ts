/// <reference types="vitest" />
import { defineConfig, mergeConfig } from 'vite'
import { defineConfig as defineVitestConfig } from 'vitest/config'
import react from '@vitejs/plugin-react-swc'
import tailwindcss from '@tailwindcss/vite'
import path from 'path'

export default mergeConfig(
  defineConfig({
    plugins: [react(), tailwindcss()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
  }),
  defineVitestConfig({
    test: {
      globals: true,
      environment: 'jsdom',
      setupFiles: './src/setupTests.ts',
      css: true,
      coverage: {
        provider: 'v8',
        reporter: ['text', 'json', 'html'],
        reportsDirectory: './coverage',
        include: [
          'src/api/**/*.{ts,tsx}', // API 서비스 & 클라이언트
          'src/components/!(ui)/**/*.{ts,tsx}', // shadcn/ui 제외한 모든 컴포넌트
          'src/lib/**/*.{ts,tsx}', // 유틸리티 & 로직
          'src/pages/**/*.{ts,tsx}', // 페이지 컴포넌트
          'src/utils/!(mockData).ts', // mockData 제외한 유틸
        ],
        // 커버리지에서 제외할 파일
        exclude: [
          '**/*.test.{ts,tsx}',
          '**/*.spec.{ts,tsx}',
          '**/node_modules/**',
          '**/dist/**',
          '**/coverage/**',
          'src/setupTests.ts',
          'src/test-utils/**',
          'src/main.tsx',
          'src/router.tsx',
          'src/types/**', // 타입 정의 파일
          'src/components/ui/**', // shadcn/ui
          'src/hooks/use-mobile.ts',
          'src/lib/query-client.ts',
          'src/lib/sidebar/types.ts', // 타입 정의
          'src/utils/mockData.ts', // 목 데이터
        ],
      },
      include: ['src/**/*.{test,spec}.{js,jsx,ts,tsx}'],
      exclude: ['node_modules', 'dist'],
    },
  })
)
