import { expect, test } from '@playwright/test';
import { createUserViaAdmin, signIn, uniqueUsername } from '../helpers';

test('sign-out redirects to guest home', async ({ page }) => {
  const username = uniqueUsername();
  await createUserViaAdmin(page, username);
  await signIn(page, username);
  await page.goto('/auth/sign-out');
  await expect(page).toHaveURL('/');
  await expect(page.getByRole('link', { name: 'Sign in' }).first()).toBeVisible();
});

test('after sign-out, profile redirects to home', async ({ page }) => {
  const username = uniqueUsername();
  await createUserViaAdmin(page, username);
  await signIn(page, username);
  await page.goto('/auth/sign-out');
  await page.goto('/profile');
  await expect(page).toHaveURL('/');
});

test('after sign-out, admin redirects to home', async ({ page }) => {
  const username = uniqueUsername();
  await createUserViaAdmin(page, username);
  await signIn(page, username);
  await page.goto('/auth/sign-out');
  await page.goto('/admin/user');
  await expect(page).toHaveURL('/');
});
