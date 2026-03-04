import { env } from '$env/dynamic/private';
import type { Profile } from '$lib/models/user/user';
import { deLocalizeUrl } from '$lib/paraglide/runtime.js';
import { makeRequest } from '$lib/server/apis/api';
import { HttpRequest, isAdmin } from '$lib/server/utils/util';
import { error, redirect } from '@sveltejs/kit';
import type { LayoutServerLoad } from './$types';

export const load = (async ({ locals, cookies, url }) => {
  if (!locals.userId) return { profile: null };

  const response = await makeRequest(
    {
      method: HttpRequest.GET,
      path: `/profile`,
    },
    cookies,
  );

  if ('error' in response) {
    if (response.status == 401)
      cookies.delete('accessToken', { path: '/', domain: env.DOMAIN_NAME });
    error(response.status, { message: response.error });
  }

  const profile = response as Profile;
  if (!profile.username && deLocalizeUrl(url.href).pathname !== '/profile')
    redirect(302, '/profile');

  return { profile, isAdmin: isAdmin(cookies.get('accessToken')) };
}) satisfies LayoutServerLoad;
