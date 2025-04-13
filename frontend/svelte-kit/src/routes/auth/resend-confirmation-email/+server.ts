import { makeRequest } from '$lib/server/apis/api';
import { HttpRequest } from '$lib/server/utils/util';
import { error, redirect } from '@sveltejs/kit';
import type { RequestHandler } from './$types';

export const GET = (async ({ locals, url, cookies }) => {
  if (locals.userId) redirect(302, '/');

  const usernameOrEmail = url.searchParams.get('usernameOrEmail');
  const response = await makeRequest(
    {
      method: HttpRequest.POST,
      path: '/auth/verification-email',
      body: JSON.stringify({ usernameOrEmail }),
    },
    cookies,
  );

  if ('error' in response) error(response.status, { message: response.error });

  redirect(302, '/auth/sign-in');
}) satisfies RequestHandler;
