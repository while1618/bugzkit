import { expect, test } from '@playwright/test';
import { signUp, uniqueUsername } from '../helpers';

const MAILPIT_URL = 'http://localhost:8025';

async function getVerificationToken(email: string): Promise<string> {
  const res = await fetch(`${MAILPIT_URL}/api/v1/messages`);
  const { messages } = await res.json();
  const msg = messages.find((m: { To: { Address: string }[] }) =>
    m.To.some((t) => t.Address === email),
  );
  if (!msg) throw new Error(`No email found for ${email}`);
  const bodyRes = await fetch(`${MAILPIT_URL}/api/v1/message/${msg.ID}`);
  const { HTML } = await bodyRes.json();
  const match = HTML.match(/verify-email\?token=([a-f0-9-]+)/);
  if (!match) throw new Error('Verification token not found in email');
  return match[1];
}

test('verify-email with invalid token shows error page', async ({ page }) => {
  await page.goto('/auth/verify-email?token=invalid-token');
  await expect(page.getByRole('heading', { name: '400' })).toBeVisible();
  await expect(page.getByText('Bad Request')).toBeVisible();
});

test('verify-email with valid token redirects to sign-in', async ({ page }) => {
  const username = uniqueUsername();
  await signUp(page, username);

  const token = await getVerificationToken(`${username}@example.com`);
  await page.goto(`/auth/verify-email?token=${token}`);
  await expect(page).toHaveURL('/auth/sign-in');
});

test.describe('already authenticated', () => {
  test.use({ storageState: 'tests/.auth/user.json' });

  test('already authenticated user is redirected from verify-email', async ({ page }) => {
    await page.goto('/auth/verify-email');
    await expect(page).toHaveURL('/');
  });
});
