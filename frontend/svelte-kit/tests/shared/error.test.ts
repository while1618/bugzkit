import { expect, test } from '@playwright/test';

test('404 error page shows status code and homepage link', async ({ page }) => {
  await page.goto('/nonexistent-route-12345');
  await expect(page.getByRole('heading', { name: '404' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Homepage' })).toBeVisible();
});

test('404 homepage link navigates to home', async ({ page }) => {
  await page.goto('/nonexistent-route-12345');
  await page.getByRole('link', { name: 'Homepage' }).click();
  await expect(page).toHaveURL('/');
});
