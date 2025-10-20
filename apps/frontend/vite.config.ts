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
          'src/components/layout/**/*.{ts,tsx}',
          'src/lib/**/*.{ts,tsx}',
          'src/pages/**/*.{ts,tsx}',
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
          'src/components/ui/**', // shadcn/ui 제외
          'src/hooks/use-mobile.ts',
          'src/lib/query-client.ts',
        ],
      },
      include: ['src/**/*.{test,spec}.{js,jsx,ts,tsx}'],
      exclude: ['node_modules', 'dist'],
    },
  })
)
