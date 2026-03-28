import { expect, test } from '@playwright/test';
import { signUp, uniqueUsername } from '../helpers';

const MAILPIT_URL = 'http://localhost:8025';

async function getResetToken(email: string): Promise<string> {
  const res = await fetch(`${MAILPIT_URL}/api/v1/messages`);
  const { messages } = await res.json();
  const msg = messages.find((m: { To: { Address: string }[] }) =>
    m.To.some((t) => t.Address === email),
  );
  if (!msg) throw new Error(`No email found for ${email}`);
  const bodyRes = await fetch(`${MAILPIT_URL}/api/v1/message/${msg.ID}`);
  const { HTML } = await bodyRes.json();
  const match = HTML.match(/reset-password\?token=([a-f0-9-]+)/);
  if (!match) throw new Error('Reset token not found in email');
  return match[1];
}

test('reset-password page renders form', async ({ page }) => {
  await page.goto('/auth/reset-password');
  await expect(page.locator('[data-slot="card-title"]')).toContainText('Reset password');
  await expect(page.locator('input[name="password"]')).toBeVisible();
  await expect(page.locator('input[name="confirmPassword"]')).toBeVisible();
  await expect(page.locator('form [type="submit"]')).toBeVisible();
});

test('reset-password shows validation errors on empty submit', async ({ page }) => {
  await page.goto('/auth/reset-password');
  await page.locator('form [type="submit"]').click();
  await expect(page.getByText('Invalid password').first()).toBeVisible();
});

test('reset-password shows error for password mismatch', async ({ page }) => {
  await page.goto('/auth/reset-password');
  await page.locator('input[name="password"]').fill('qwerty123');
  await page.locator('input[name="confirmPassword"]').fill('qwerty456');
  await page.locator('form [type="submit"]').click();
  await expect(page.getByText('Passwords do not match')).toBeVisible();
});

test('reset-password with invalid token shows error', async ({ page }) => {
  await page.goto('/auth/reset-password?token=invalid-token');
  await page.locator('input[name="password"]').fill('qwerty123');
  await page.locator('input[name="confirmPassword"]').fill('qwerty123');
  await page.locator('form [type="submit"]').click();
  await expect(page.getByText('Invalid token')).toBeVisible();
});

test('reset-password with valid token redirects to sign-in', async ({ page }) => {
  const username = uniqueUsername();
  await signUp(page, username);

  await page.goto('/auth/forgot-password');
  await page.locator('input[name="email"]').fill(`${username}@example.com`);
  await page.locator('form [type="submit"]').click();
  await expect(page.getByText('Forgot password email sent successfully')).toBeVisible();

  const token = await getResetToken(`${username}@example.com`);
  await page.goto(`/auth/reset-password?token=${token}`);
  await page.locator('input[name="password"]').fill('qwerty123');
  await page.locator('input[name="confirmPassword"]').fill('qwerty123');
  await page.locator('form [type="submit"]').click();
  await expect(page).toHaveURL('/auth/sign-in');
});

test.describe('already authenticated', () => {
  test.use({ storageState: 'tests/.auth/user.json' });

  test('already authenticated user is redirected from reset-password', async ({ page }) => {
    await page.goto('/auth/reset-password');
    await expect(page).toHaveURL('/');
  });
});
