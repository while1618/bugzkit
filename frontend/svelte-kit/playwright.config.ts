/// <reference types="node" />
import type { PlaywrightTestConfig } from '@playwright/test';

const config: PlaywrightTestConfig = {
  globalSetup: './tests/setup.ts',
  globalTeardown: './tests/cleanup.ts',
  webServer: {
    command: 'pnpm run build && pnpm run preview',
    port: 4173,
  },
  use: {
    browserName: 'firefox',
    baseURL: 'http://localhost:4173',
  },
  testDir: 'tests',
  testMatch: /(.+\.)?(test|spec)\.[jt]s/,
  workers: 1,
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
