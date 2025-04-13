import type { Profile } from '$lib/models/user/user';
import { makeRequest } from '$lib/server/apis/api';
import { HttpRequest, isAdmin } from '$lib/server/utils/util';
import { error } from '@sveltejs/kit';
import type { LayoutServerLoad } from './$types';

export const load = (async ({ locals, cookies }) => {
  if (!locals.userId) return { profile: null };

  const response = await makeRequest(
    {
      method: HttpRequest.GET,
      path: `/profile`,
    },
    cookies,
  );

  if ('error' in response) error(response.status, { message: response.error });

  return { profile: response as Profile, isAdmin: isAdmin(cookies.get('accessToken')) };
}) satisfies LayoutServerLoad;
