import { expect, test } from '@playwright/test';
import { findUserInAdminTable, uniqueUsername } from '../helpers';

const userFile = 'tests/.auth/user.json';
const adminFile = 'tests/.auth/admin.json';

test.describe('as admin', () => {
  test.use({ storageState: adminFile });

  test('admin user can access admin user list', async ({ page }) => {
    await page.goto('/admin/user');
    await expect(page).toHaveURL('/admin/user');
    await expect(page.getByRole('columnheader', { name: 'User ID' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Username' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Email' })).toBeVisible();
  });

  test('admin page shows admin username in the table', async ({ page }) => {
    await page.goto('/admin/user');
    await expect(page.getByRole('link', { name: 'admin' })).toBeVisible();
  });

  test('create user dialog opens with form fields', async ({ page }) => {
    await page.goto('/admin/user');
    await page.getByRole('button', { name: 'Create user' }).click();
    await expect(page.locator('input[name="username"]')).toBeVisible();
    await expect(page.locator('input[name="email"]')).toBeVisible();
    await expect(page.locator('input[name="password"]')).toBeVisible();
    await expect(page.locator('input[name="confirmPassword"]')).toBeVisible();
  });

  test('create user success shows toast and user appears in table', async ({ page }) => {
    const username = uniqueUsername();
    await page.goto('/admin/user');
    await page.getByRole('button', { name: 'Create user' }).click();
    await page.locator('input[name="username"]').fill(username);
    await page.locator('input[name="email"]').fill(`${username}@example.com`);
    await page.locator('input[name="password"]').fill('qwerty123');
    await page.locator('input[name="confirmPassword"]').fill('qwerty123');
    await page.getByRole('button', { name: 'Save' }).click();
    await expect(page.getByText('User created successfully')).toBeVisible();
    await findUserInAdminTable(page, username);
  });

  test('delete user opens confirmation dialog', async ({ page }) => {
    const username = uniqueUsername();
    await page.goto('/admin/user');
    await page.getByRole('button', { name: 'Create user' }).click();
    await page.locator('input[name="username"]').fill(username);
    await page.locator('input[name="email"]').fill(`${username}@example.com`);
    await page.locator('input[name="password"]').fill('qwerty123');
    await page.locator('input[name="confirmPassword"]').fill('qwerty123');
    await page.getByRole('button', { name: 'Save' }).click();
    await expect(page.getByText('User created successfully')).toBeVisible();

    const userRow = await findUserInAdminTable(page, username);
    await userRow.getByRole('button').last().click();
    await expect(page.getByText(`Are you sure you want to delete ${username}?`)).toBeVisible();
  });

  test('delete user success shows toast and removes from table', async ({ page }) => {
    const username = uniqueUsername();
    await page.goto('/admin/user');
    await page.getByRole('button', { name: 'Create user' }).click();
    await page.locator('input[name="username"]').fill(username);
    await page.locator('input[name="email"]').fill(`${username}@example.com`);
    await page.locator('input[name="password"]').fill('qwerty123');
    await page.locator('input[name="confirmPassword"]').fill('qwerty123');
    await page.getByRole('button', { name: 'Save' }).click();
    await expect(page.getByText('User created successfully')).toBeVisible();

    const userRow = await findUserInAdminTable(page, username);
    await userRow.getByRole('button').last().click();
    await page.getByRole('button', { name: 'Delete' }).click();
    await expect(page.getByText('User deleted successfully')).toBeVisible();
    await expect(page.getByRole('link', { name: username })).not.toBeVisible();
  });

  test('cannot activate self shows toast', async ({ page }) => {
    await page.goto('/admin/user');
    const adminRow = page.getByRole('row').filter({ hasText: 'admin' });
    await adminRow.getByRole('button').nth(0).click();
    await expect(page.getByText('You cannot modify this field for your own account')).toBeVisible();
  });

  test('cannot lock self shows toast', async ({ page }) => {
    await page.goto('/admin/user');
    const adminRow = page.getByRole('row').filter({ hasText: 'admin' });
    await adminRow.getByRole('button').nth(1).click();
    await expect(page.getByText('You cannot modify this field for your own account')).toBeVisible();
  });

  test('cannot change roles for self shows toast', async ({ page }) => {
    await page.goto('/admin/user');
    const adminRow = page.getByRole('row').filter({ hasText: 'admin' });
    await adminRow.getByRole('button').nth(2).click();
    await expect(page.getByText('You cannot modify this field for your own account')).toBeVisible();
  });

  test('deactivate and activate user', async ({ page }) => {
    const username = uniqueUsername();
    await page.goto('/admin/user');
    await page.getByRole('button', { name: 'Create user' }).click();
    await page.locator('input[name="username"]').fill(username);
    await page.locator('input[name="email"]').fill(`${username}@example.com`);
    await page.locator('input[name="password"]').fill('qwerty123');
    await page.locator('input[name="confirmPassword"]').fill('qwerty123');
    await page.getByRole('button', { name: 'Save' }).click();
    await expect(page.getByText('User created successfully')).toBeVisible();

    const userRow = await findUserInAdminTable(page, username);
    await userRow.getByRole('button').nth(0).click();
    await expect(page.getByText(`Are you sure you want to deactivate ${username}?`)).toBeVisible();
    await page.getByRole('button', { name: 'Deactivate' }).click();
    await expect(page.getByText('User deactivated successfully')).toBeVisible();

    await userRow.getByRole('button').nth(0).click();
    await expect(page.getByText(`Are you sure you want to activate ${username}?`)).toBeVisible();
    await page.getByRole('button', { name: 'Activate' }).click();
    await expect(page.getByText('User activated successfully')).toBeVisible();
  });

  test('lock and unlock user', async ({ page }) => {
    const username = uniqueUsername();
    await page.goto('/admin/user');
    await page.getByRole('button', { name: 'Create user' }).click();
    await page.locator('input[name="username"]').fill(username);
    await page.locator('input[name="email"]').fill(`${username}@example.com`);
    await page.locator('input[name="password"]').fill('qwerty123');
    await page.locator('input[name="confirmPassword"]').fill('qwerty123');
    await page.getByRole('button', { name: 'Save' }).click();
    await expect(page.getByText('User created successfully')).toBeVisible();

    const userRow = await findUserInAdminTable(page, username);
    await userRow.getByRole('button').nth(1).click();
    await expect(page.getByText(`Are you sure you want to lock ${username}?`)).toBeVisible();
    await page.getByRole('button', { name: 'Lock' }).click();
    await expect(page.getByText('User locked successfully')).toBeVisible();

    await userRow.getByRole('button').nth(1).click();
    await expect(page.getByText(`Are you sure you want to unlock ${username}?`)).toBeVisible();
    await page.getByRole('button', { name: 'Unlock' }).click();
    await expect(page.getByText('User unlocked successfully')).toBeVisible();
  });

  test('change roles dialog opens with roles checkboxes', async ({ page }) => {
    const username = uniqueUsername();
    await page.goto('/admin/user');
    await page.getByRole('button', { name: 'Create user' }).click();
    await page.locator('input[name="username"]').fill(username);
    await page.locator('input[name="email"]').fill(`${username}@example.com`);
    await page.locator('input[name="password"]').fill('qwerty123');
    await page.locator('input[name="confirmPassword"]').fill('qwerty123');
    await page.getByRole('button', { name: 'Save' }).click();
    await expect(page.getByText('User created successfully')).toBeVisible();

    const userRow = await findUserInAdminTable(page, username);
    await userRow.getByRole('button').nth(2).click();
    await expect(page.getByText(`Select roles for ${username}`)).toBeVisible();
    await expect(page.getByLabel('USER')).toBeVisible();
    await expect(page.getByLabel('ADMIN')).toBeVisible();
  });

  test('create user shows error for invalid username', async ({ page }) => {
    await page.goto('/admin/user');
    await page.getByRole('button', { name: 'Create user' }).click();
    await page.locator('input[name="username"]').fill('invalid username!');
    await page.locator('input[name="email"]').fill('valid@example.com');
    await page.locator('input[name="password"]').fill('qwerty123');
    await page.locator('input[name="confirmPassword"]').fill('qwerty123');
    await page.getByRole('button', { name: 'Save' }).click();
    await expect(page.getByText('Invalid username')).toBeVisible();
  });

  test('create user shows error for invalid email', async ({ page }) => {
    await page.goto('/admin/user');
    await page.getByRole('button', { name: 'Create user' }).click();
    await page.locator('input[name="username"]').fill('validuser');
    await page.locator('input[name="email"]').fill('not-an-email');
    await page.locator('input[name="password"]').fill('qwerty123');
    await page.locator('input[name="confirmPassword"]').fill('qwerty123');
    await page.getByRole('button', { name: 'Save' }).click();
    await expect(page.getByText('Invalid email')).toBeVisible();
  });

  test('create user shows error for invalid password', async ({ page }) => {
    await page.goto('/admin/user');
    await page.getByRole('button', { name: 'Create user' }).click();
    await page.locator('input[name="username"]').fill('validuser');
    await page.locator('input[name="email"]').fill('valid@example.com');
    await page.locator('input[name="password"]').fill('short');
    await page.locator('input[name="confirmPassword"]').fill('short');
    await page.getByRole('button', { name: 'Save' }).click();
    await expect(page.getByText('Invalid password').first()).toBeVisible();
  });

  test('create user shows error for password mismatch', async ({ page }) => {
    await page.goto('/admin/user');
    await page.getByRole('button', { name: 'Create user' }).click();
    await page.locator('input[name="username"]').fill('validuser');
    await page.locator('input[name="email"]').fill('valid@example.com');
    await page.locator('input[name="password"]').fill('qwerty123');
    await page.locator('input[name="confirmPassword"]').fill('qwerty456');
    await page.getByRole('button', { name: 'Save' }).click();
    await expect(page.getByText('Passwords do not match')).toBeVisible();
  });

  test('pagination previous button is disabled on first page', async ({ page }) => {
    await page.goto('/admin/user?page=1&size=1');
    await expect(page.getByRole('button', { name: /Previous/ })).toBeDisabled();
  });

  test('pagination next button navigates to next page', async ({ page }) => {
    await page.goto('/admin/user?page=1&size=1');
    await page.getByRole('link', { name: /Next/ }).click();
    await expect(page).toHaveURL(/page=2/);
  });

  test('pagination invalid page param defaults to page 1', async ({ page }) => {
    await page.goto('/admin/user?page=0&size=10');
    await expect(page.getByRole('button', { name: /Previous/ })).toBeDisabled();
  });

  test('change roles success shows toast', async ({ page }) => {
    const username = uniqueUsername();
    await page.goto('/admin/user');
    await page.getByRole('button', { name: 'Create user' }).click();
    await page.locator('input[name="username"]').fill(username);
    await page.locator('input[name="email"]').fill(`${username}@example.com`);
    await page.locator('input[name="password"]').fill('qwerty123');
    await page.locator('input[name="confirmPassword"]').fill('qwerty123');
    await page.getByRole('button', { name: 'Save' }).click();
    await expect(page.getByText('User created successfully')).toBeVisible();

    const userRow = await findUserInAdminTable(page, username);
    await userRow.getByRole('button').nth(2).click();
    await page.getByLabel('ADMIN').check();
    await page.getByRole('button', { name: 'Save' }).click();
    await expect(page.getByText('Roles changed successfully')).toBeVisible();
  });
});

test.describe('as regular user', () => {
  test.use({ storageState: userFile });

  test('regular user is redirected from admin', async ({ page }) => {
    await page.goto('/admin/user');
    await expect(page).toHaveURL('/');
  });
});
