import { expect, test } from '@playwright/test';
import { signIn } from '../helpers';

test('sign-out redirects to guest home', async ({ page }) => {
  await signIn(page, 'user');
  await page.goto('/auth/sign-out');
  await expect(page).toHaveURL('/');
  await expect(page.getByRole('link', { name: 'Sign in' }).first()).toBeVisible();
});

test('after sign-out, profile redirects to home', async ({ page }) => {
  await signIn(page, 'user');
  await page.goto('/auth/sign-out');
  await page.goto('/profile');
  await expect(page).toHaveURL('/');
});

test('after sign-out, admin redirects to home', async ({ page }) => {
  await signIn(page, 'admin');
  await page.goto('/auth/sign-out');
  await page.goto('/admin/user');
  await expect(page).toHaveURL('/');
});
