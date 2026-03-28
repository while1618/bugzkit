import { test as setup } from '@playwright/test';

const userFile = 'tests/.auth/user.json';
const adminFile = 'tests/.auth/admin.json';

setup('authenticate as user', async ({ page }) => {
  await page.goto('/auth/sign-in');
  await page.locator('input[name="usernameOrEmail"]').fill('user');
  await page.locator('input[name="password"]').fill('qwerty123');
  await page.locator('form [type="submit"]').click();
  await page.waitForURL('/');
  await page.context().storageState({ path: userFile });
});

setup('authenticate as admin', async ({ page }) => {
  await page.goto('/auth/sign-in');
  await page.locator('input[name="usernameOrEmail"]').fill('admin');
  await page.locator('input[name="password"]').fill('qwerty123');
  await page.locator('form [type="submit"]').click();
  await page.waitForURL('/');
  await page.context().storageState({ path: adminFile });
});
