/// <reference types="node" />
import type { PlaywrightTestConfig } from '@playwright/test';

const config: PlaywrightTestConfig = {
  reporter: [['list'], ['github'], ['html', { open: 'never' }]],
  globalSetup: './tests/setup.ts',
  globalTeardown: './tests/cleanup.ts',
  webServer: {
    command: 'pnpm run build && pnpm run preview',
    port: 4173,
  },
  use: {
    browserName: 'firefox',
    baseURL: 'http://localhost:4173',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  testDir: 'tests',
  testMatch: /(.+\.)?(test|spec)\.[jt]s/,
  workers: 4,
  projects: [
    {
      name: 'setup',
      testMatch: /auth\.setup\.[jt]s/,
    },
    {
      name: 'tests',
      dependencies: ['setup'],
    },
  ],
};

export default config;
