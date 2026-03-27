import { expect, test } from '@playwright/test';

test('redirects unauthenticated user from /profile to home', async ({ page }) => {
  await page.goto('/profile');
  await expect(page).toHaveURL('/');
});

test('redirects unauthenticated user from /admin to home', async ({ page }) => {
  await page.goto('/admin/user');
  await expect(page).toHaveURL('/');
});

test('redirects unauthenticated user from /auth/sign-out to home', async ({ page }) => {
  await page.goto('/auth/sign-out');
  await expect(page).toHaveURL('/');
});

test('redirects unauthenticated user from /auth/sign-out-from-all-devices to home', async ({
  page,
}) => {
  await page.goto('/auth/sign-out-from-all-devices');
  await expect(page).toHaveURL('/');
});
