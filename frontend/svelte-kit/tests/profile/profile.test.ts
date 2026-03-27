import { expect, test } from '@playwright/test';

const userFile = 'tests/.auth/user.json';
const adminFile = 'tests/.auth/admin.json';

test.describe('as user', () => {
  test.use({ storageState: userFile });

  test('authenticated user can access profile page', async ({ page }) => {
    await page.goto('/profile');
    await expect(page).toHaveURL('/profile');
    await expect(page.getByRole('heading', { name: 'user' })).toBeVisible();
  });
});

test.describe('as admin', () => {
  test.use({ storageState: adminFile });

  test('profile page shows username as heading', async ({ page }) => {
    await page.goto('/profile');
    await expect(page.getByRole('heading', { name: 'admin' })).toBeVisible();
  });
});
