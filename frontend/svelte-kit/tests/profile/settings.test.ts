import { expect, test } from '@playwright/test';
import { createUserViaAdmin, signIn, uniqueUsername } from '../helpers';

const userFile = 'tests/.auth/user.json';

test('unauthenticated user is redirected from settings to home', async ({ page }) => {
  await page.goto('/profile/settings');
  await expect(page).toHaveURL('/');
});

test.describe('as user', () => {
  test.use({ storageState: userFile });

  test('settings page shows three tabs', async ({ page }) => {
    await page.goto('/profile/settings');
    await expect(page.getByRole('tab', { name: 'Account' })).toBeVisible();
    await expect(page.getByRole('tab', { name: 'Password' })).toBeVisible();
    await expect(page.getByRole('tab', { name: 'Devices' })).toBeVisible();
  });

  test('account tab prefills username and email', async ({ page }) => {
    await page.goto('/profile/settings');
    await expect(page.locator('input[name="username"]')).toHaveValue('user');
    await expect(page.locator('input[name="email"]')).toHaveValue('user@localhost');
  });

  test('account tab shows delete account button', async ({ page }) => {
    await page.goto('/profile/settings');
    await expect(page.getByRole('button', { name: 'Delete account' })).toBeVisible();
  });

  test('delete account button opens confirmation dialog', async ({ page }) => {
    await page.goto('/profile/settings');
    await page.getByRole('button', { name: 'Delete account' }).click();
    await expect(
      page.getByText('Are you sure you want to delete your account? This action cannot be undone'),
    ).toBeVisible();
    await expect(page.getByRole('button', { name: 'Delete', exact: true })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Cancel' })).toBeVisible();
  });

  test('delete confirmation cancel closes dialog', async ({ page }) => {
    await page.goto('/profile/settings');
    await page.getByRole('button', { name: 'Delete account' }).click();
    await page.getByRole('button', { name: 'Cancel' }).click();
    await expect(
      page.getByText('Are you sure you want to delete your account? This action cannot be undone'),
    ).not.toBeVisible();
  });

  test('devices tab shows current device', async ({ page }) => {
    await page.goto('/profile/settings');
    await page.getByRole('tab', { name: 'Devices' }).click();
    await expect(page.getByText('(current)')).toBeVisible();
  });

  test('devices tab shows sign out from all devices button', async ({ page }) => {
    await page.goto('/profile/settings');
    await page.getByRole('tab', { name: 'Devices' }).click();
    await expect(page.getByRole('link', { name: 'Sign out from all devices' })).toBeVisible();
  });
});

test.describe('as user - validation errors', () => {
  test.use({ storageState: userFile });

  test('account tab shows error for duplicate username', async ({ page }) => {
    await page.goto('/profile/settings');
    await page.locator('input[name="username"]').fill('admin');
    await page.getByRole('button', { name: 'Save' }).first().click();
    await expect(page.getByText('Username already exists')).toBeVisible();
  });

  test('account tab shows error for duplicate email', async ({ page }) => {
    await page.goto('/profile/settings');
    await page.locator('input[name="email"]').fill('office@bugzkit.com');
    await page.getByRole('button', { name: 'Save' }).first().click();
    await expect(page.getByText('Email already exists')).toBeVisible();
  });

  test('password tab shows error for wrong current password', async ({ page }) => {
    await page.goto('/profile/settings');
    await page.getByRole('tab', { name: 'Password' }).click();
    await page.locator('input[name="currentPassword"]').fill('wrongpassword1');
    await page.locator('input[name="newPassword"]').fill('newpassword123');
    await page.locator('input[name="confirmNewPassword"]').fill('newpassword123');
    await page.getByRole('button', { name: 'Save' }).click();
    await expect(page.getByText('Current password is wrong')).toBeVisible();
  });

  test('password tab shows error when passwords do not match', async ({ page }) => {
    await page.goto('/profile/settings');
    await page.getByRole('tab', { name: 'Password' }).click();
    await page.locator('input[name="currentPassword"]').fill('qwerty123');
    await page.locator('input[name="newPassword"]').fill('newpassword123');
    await page.locator('input[name="confirmNewPassword"]').fill('different456');
    await page.getByRole('button', { name: 'Save' }).click();
    await expect(page.getByText('Passwords do not match')).toBeVisible();
  });
});

test.describe('settings with fresh user', () => {
  test('update username success shows toast', async ({ page }) => {
    const username = uniqueUsername();
    await createUserViaAdmin(page, username);
    await signIn(page, username);

    await page.goto('/profile/settings');
    const newUsername = uniqueUsername();
    await page.locator('input[name="username"]').fill(newUsername);
    await page.getByRole('button', { name: 'Save' }).first().click();
    await expect(page.getByText('Profile updated successfully')).toBeVisible();
  });

  test('change password success shows toast', async ({ page }) => {
    const username = uniqueUsername();
    await createUserViaAdmin(page, username);
    await signIn(page, username);

    await page.goto('/profile/settings');
    await page.getByRole('tab', { name: 'Password' }).click();
    await page.locator('input[name="currentPassword"]').fill('qwerty123');
    await page.locator('input[name="newPassword"]').fill('newpassword123');
    await page.locator('input[name="confirmNewPassword"]').fill('newpassword123');
    await page.getByRole('button', { name: 'Save' }).click();
    await expect(page.getByText('Password changed successfully')).toBeVisible();
  });

  test('revoke a device shows confirmation and removes it', async ({ page, browser }) => {
    const username = uniqueUsername();
    await createUserViaAdmin(page, username);
    await signIn(page, username);

    const secondContext = await browser.newContext();
    const secondPage = await secondContext.newPage();
    await signIn(secondPage, username);
    await secondPage.close();
    await secondContext.close();

    await page.goto('/profile/settings');
    await page.getByRole('tab', { name: 'Devices' }).click();
    await page.getByRole('button', { name: 'Revoke' }).first().click();
    await expect(
      page.getByText('Are you sure you want to revoke this device session?'),
    ).toBeVisible();
    await page.getByRole('button', { name: 'Revoke' }).last().click();
    await expect(page.getByText('Device revoked successfully')).toBeVisible();
  });

  test('delete account redirects to home', async ({ page }) => {
    const username = uniqueUsername();
    await createUserViaAdmin(page, username);
    await signIn(page, username);

    await page.goto('/profile/settings');
    await page.getByRole('button', { name: 'Delete account' }).click();
    await page.getByRole('button', { name: 'Delete', exact: true }).click();
    await expect(page).toHaveURL('/');
    await expect(page.getByRole('link', { name: 'Sign in' }).first()).toBeVisible();
  });

  test('sign out from all devices redirects to home', async ({ page }) => {
    const username = uniqueUsername();
    await createUserViaAdmin(page, username);
    await signIn(page, username);

    await page.goto('/profile/settings');
    await page.getByRole('tab', { name: 'Devices' }).click();
    await page.getByRole('link', { name: 'Sign out from all devices' }).click();
    await expect(page).toHaveURL('/');
    await expect(page.getByRole('link', { name: 'Sign in' }).first()).toBeVisible();
  });
});
