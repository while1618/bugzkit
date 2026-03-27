import { expect, test } from '@playwright/test';

test('public user profile shows username heading', async ({ page }) => {
  await page.goto('/user/user');
  await expect(page.getByRole('heading', { name: 'user' })).toBeVisible();
});

test('public user profile shows admin username heading', async ({ page }) => {
  await page.goto('/user/admin');
  await expect(page.getByRole('heading', { name: 'admin' })).toBeVisible();
});

test('non-existent user profile returns 404', async ({ page }) => {
  const response = await page.goto('/user/nonexistentuser12345');
  expect(response?.status()).toBe(404);
});
