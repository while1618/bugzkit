import { expect, test } from '@playwright/test';

test.describe('as authenticated user', () => {
  test.use({ storageState: 'tests/.auth/user.json' });

  test('shows user home for authenticated users', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('heading', { name: 'Hello there, user' })).toBeVisible();
  });

  test('authenticated home does not show sign-in or sign-up links', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('link', { name: 'Sign in' })).not.toBeVisible();
    await expect(page.getByRole('link', { name: 'Sign up' })).not.toBeVisible();
  });
});

test('shows guest home for unauthenticated users', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByRole('link', { name: 'Sign in' }).first()).toBeVisible();
  await expect(page.getByRole('link', { name: 'Sign up' }).first()).toBeVisible();
});

test('sign in link navigates to sign-in page', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('link', { name: 'Sign in' }).first().click();
  await expect(page).toHaveURL('/auth/sign-in');
});

test('sign up link navigates to sign-up page', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('link', { name: 'Sign up' }).first().click();
  await expect(page).toHaveURL('/auth/sign-up');
});
