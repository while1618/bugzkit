import { expect, test } from '@playwright/test';
import { signUp, uniqueUsername } from '../helpers';

test('resend-confirmation-email with non-existent user redirects to sign-in', async ({ page }) => {
  await page.goto('/auth/resend-confirmation-email?usernameOrEmail=doesnotexist');
  await expect(page).toHaveURL('/auth/sign-in');
});

test('resend-confirmation-email with already active user redirects to sign-in', async ({
  page,
}) => {
  await page.goto('/auth/resend-confirmation-email?usernameOrEmail=user');
  await expect(page).toHaveURL('/auth/sign-in');
});

test('resend-confirmation-email with inactive user redirects to sign-in', async ({ page }) => {
  const username = uniqueUsername();
  await signUp(page, username);

  await page.goto(`/auth/resend-confirmation-email?usernameOrEmail=${username}`);
  await expect(page).toHaveURL('/auth/sign-in');
});

test.describe('already authenticated', () => {
  test.use({ storageState: 'tests/.auth/user.json' });

  test('already authenticated user is redirected from resend-confirmation-email', async ({
    page,
  }) => {
    await page.goto('/auth/resend-confirmation-email');
    await expect(page).toHaveURL('/');
  });
});
