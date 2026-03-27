import { expect, test } from '@playwright/test';

test('forgot-password page renders form', async ({ page }) => {
  await page.goto('/auth/forgot-password');
  await expect(page.locator('[data-slot="card-title"]')).toContainText('Forgot password');
  await expect(page.locator('input[name="email"]')).toBeVisible();
  await expect(page.locator('form [type="submit"]')).toBeVisible();
});

test('forgot-password shows validation error for invalid email', async ({ page }) => {
  await page.goto('/auth/forgot-password');
  await page.locator('input[name="email"]').fill('not-an-email');
  await page.locator('form [type="submit"]').click();
  await expect(page.getByText('Invalid email')).toBeVisible();
});

test('forgot-password shows success toast', async ({ page }) => {
  await page.goto('/auth/forgot-password');
  await page.locator('input[name="email"]').fill('nonexistent@example.com');
  await page.locator('form [type="submit"]').click();
  await expect(page.getByText('Forgot password email sent successfully')).toBeVisible();
});
