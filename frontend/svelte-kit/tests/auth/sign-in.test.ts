import { expect, test } from '@playwright/test';

test('sign-in page renders form', async ({ page }) => {
  await page.goto('/auth/sign-in');
  await expect(page.locator('[data-slot="card-title"]')).toContainText('Sign in');
  await expect(page.locator('input[name="usernameOrEmail"]')).toBeVisible();
  await expect(page.locator('input[name="password"]')).toBeVisible();
  await expect(page.locator('form [type="submit"]')).toBeVisible();
});

test('sign-in shows validation errors on empty submit', async ({ page }) => {
  await page.goto('/auth/sign-in');
  await page.locator('form [type="submit"]').click();
  await expect(page.getByText('Invalid username or email')).toBeVisible();
  await expect(page.getByText('Invalid password')).toBeVisible();
});

test('sign-in shows validation error for invalid input', async ({ page }) => {
  await page.goto('/auth/sign-in');
  await page.locator('input[name="usernameOrEmail"]').fill('invalid#$%');
  await page.locator('input[name="password"]').fill('short');
  await page.locator('form [type="submit"]').click();
  await expect(page.getByText('Invalid username or email')).toBeVisible();
  await expect(page.getByText('Invalid password')).toBeVisible();
});

test('sign-in has link to sign-up page', async ({ page }) => {
  await page.goto('/auth/sign-in');
  await page.getByRole('link', { name: 'Sign up' }).click();
  await expect(page).toHaveURL('/auth/sign-up');
});

test('sign-in has link to forgot password page', async ({ page }) => {
  await page.goto('/auth/sign-in');
  await page.getByRole('link', { name: 'Forgot password?' }).click();
  await expect(page).toHaveURL('/auth/forgot-password');
});

test('sign-in with valid username redirects to home', async ({ page }) => {
  await page.goto('/auth/sign-in');
  await page.locator('input[name="usernameOrEmail"]').fill('user');
  await page.locator('input[name="password"]').fill('qwerty123');
  await page.locator('form [type="submit"]').click();
  await expect(page).toHaveURL('/');
  await expect(page.getByText('Hello there, user')).toBeVisible();
});

test('sign-in with valid email redirects to home', async ({ page }) => {
  await page.goto('/auth/sign-in');
  await page.locator('input[name="usernameOrEmail"]').fill('user@localhost');
  await page.locator('input[name="password"]').fill('qwerty123');
  await page.locator('form [type="submit"]').click();
  await expect(page).toHaveURL('/');
});

test('sign-in with wrong password stays on sign-in page', async ({ page }) => {
  await page.goto('/auth/sign-in');
  await page.locator('input[name="usernameOrEmail"]').fill('user');
  await page.locator('input[name="password"]').fill('wrongpassword1');
  await page.locator('form [type="submit"]').click();
  await expect(page).toHaveURL('/auth/sign-in');
});

test('sign-in with non-existent user stays on sign-in page', async ({ page }) => {
  await page.goto('/auth/sign-in');
  await page.locator('input[name="usernameOrEmail"]').fill('doesnotexist');
  await page.locator('input[name="password"]').fill('qwerty123');
  await page.locator('form [type="submit"]').click();
  await expect(page).toHaveURL('/auth/sign-in');
});

test.describe('already authenticated', () => {
  test.use({ storageState: 'tests/.auth/user.json' });

  test('already authenticated user is redirected from sign-in', async ({ page }) => {
    await page.goto('/auth/sign-in');
    await expect(page).toHaveURL('/');
  });
});
