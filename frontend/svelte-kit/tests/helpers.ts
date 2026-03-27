import { expect, type Page } from '@playwright/test';

export async function signIn(page: Page, usernameOrEmail: string, password = 'qwerty123') {
  await page.goto('/auth/sign-in');
  await page.locator('input[name="usernameOrEmail"]').fill(usernameOrEmail);
  await page.locator('input[name="password"]').fill(password);
  await page.locator('form [type="submit"]').click();
  await expect(page).toHaveURL('/');
}

export async function signUp(page: Page, username: string, password = 'qwerty123') {
  await page.goto('/auth/sign-up');
  await page.locator('input[name="username"]').fill(username);
  await page.locator('input[name="email"]').fill(`${username}@example.com`);
  await page.locator('input[name="password"]').fill(password);
  await page.locator('input[name="confirmPassword"]').fill(password);
  await page.locator('form [type="submit"]').click();
  await page.waitForURL('/auth/sign-in');
}

export async function createUserViaAdmin(page: Page, username: string, password = 'qwerty123') {
  await signIn(page, 'admin');
  await page.goto('/admin/user');
  await page.getByRole('button', { name: 'Create user' }).click();
  await page.locator('input[name="username"]').fill(username);
  await page.locator('input[name="email"]').fill(`${username}@example.com`);
  await page.locator('input[name="password"]').fill(password);
  await page.locator('input[name="confirmPassword"]').fill(password);
  await page.getByRole('button', { name: 'Save' }).click();
  await expect(page.getByText('User created successfully')).toBeVisible();
  await page.goto('/auth/sign-out');
}

export async function findUserInAdminTable(page: Page, username: string) {
  await page.goto('/admin/user?size=1000');
  const row = page.getByRole('row').filter({ hasText: username });
  if ((await row.count()) === 0) throw new Error(`User "${username}" not found in admin table`);
  return row;
}

export function uniqueUsername() {
  return `e2e${Date.now()}`;
}
