import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  timeout: 30000,
  expect: { timeout: 5000 },
  fullyParallel: false,
  workers: 1,
  retries: 0,
  reporter: [['html', { open: 'never' }], ['list']],
  use: {
    headless: true,
    viewport: { width: 700, height: 700 },
    actionTimeout: 5000,
  },
  webServer: {
    command: 'npx http-server . -p 8090 -s',
    port: 8090,
    reuseExistingServer: true,
    timeout: 10000,
  },
});
