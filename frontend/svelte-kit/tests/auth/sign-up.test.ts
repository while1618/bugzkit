import { expect, test } from '@playwright/test';
import { uniqueUsername } from '../helpers';

test('sign-up page renders form', async ({ page }) => {
  await page.goto('/auth/sign-up');
  await expect(page.locator('[data-slot="card-title"]')).toContainText('Sign up');
  await expect(page.locator('input[name="username"]')).toBeVisible();
  await expect(page.locator('input[name="email"]')).toBeVisible();
  await expect(page.locator('input[name="password"]')).toBeVisible();
  await expect(page.locator('input[name="confirmPassword"]')).toBeVisible();
  await expect(page.locator('form [type="submit"]')).toBeVisible();
});

test('sign-up shows validation errors on empty submit', async ({ page }) => {
  await page.goto('/auth/sign-up');
  await page.locator('form [type="submit"]').click();
  await expect(page.getByText('Invalid username')).toBeVisible();
  await expect(page.getByText('Invalid email')).toBeVisible();
  await expect(page.getByText('Invalid password').first()).toBeVisible();
});

test('sign-up shows passwords do not match error', async ({ page }) => {
  await page.goto('/auth/sign-up');
  await page.locator('input[name="username"]').fill('testuser');
  await page.locator('input[name="email"]').fill('test@example.com');
  await page.locator('input[name="password"]').fill('qwerty123');
  await page.locator('input[name="confirmPassword"]').fill('different123');
  await page.locator('form [type="submit"]').click();
  await expect(page.getByText('Passwords do not match')).toBeVisible();
});

test('sign-up has link to sign-in page', async ({ page }) => {
  await page.goto('/auth/sign-up');
  await page.getByRole('link', { name: 'Sign in' }).click();
  await expect(page).toHaveURL('/auth/sign-in');
});

test('sign-up with new credentials redirects to sign-in', async ({ page }) => {
  const username = uniqueUsername();
  await page.goto('/auth/sign-up');
  await page.locator('input[name="username"]').fill(username);
  await page.locator('input[name="email"]').fill(`${username}@example.com`);
  await page.locator('input[name="password"]').fill('qwerty123');
  await page.locator('input[name="confirmPassword"]').fill('qwerty123');
  await page.locator('form [type="submit"]').click();
  await expect(page).toHaveURL('/auth/sign-in');
});

test('sign-up with existing username shows error', async ({ page }) => {
  await page.goto('/auth/sign-up');
  await page.locator('input[name="username"]').fill('admin');
  await page.locator('input[name="email"]').fill(`${uniqueUsername()}@example.com`);
  await page.locator('input[name="password"]').fill('qwerty123');
  await page.locator('input[name="confirmPassword"]').fill('qwerty123');
  await page.locator('form [type="submit"]').click();
  await expect(page.getByText('Username already exists')).toBeVisible();
});

test('sign-up with existing email shows error', async ({ page }) => {
  await page.goto('/auth/sign-up');
  await page.locator('input[name="username"]').fill(uniqueUsername());
  await page.locator('input[name="email"]').fill('user@localhost');
  await page.locator('input[name="password"]').fill('qwerty123');
  await page.locator('input[name="confirmPassword"]').fill('qwerty123');
  await page.locator('form [type="submit"]').click();
  await expect(page.getByText('Email already exists')).toBeVisible();
});
