import type { Profile } from '$lib/models/user/user';
import { languageTag } from '$lib/paraglide/runtime';
import { makeRequest } from '$lib/server/apis/api';
import { HttpRequest, isAdmin, removeAuth } from '$lib/server/utils/util';
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
    if (response.status == 401) removeAuth(cookies, locals);
    error(response.status, { message: response.error });
  }

  const profile = response as Profile;
  if (!profile.username && url.pathname !== `/${languageTag()}/profile`) redirect(302, '/profile');

  return { profile, isAdmin: isAdmin(cookies.get('accessToken')) };
}) satisfies LayoutServerLoad;
