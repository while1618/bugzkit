import type { SimplifiedUser } from '$lib/models/user/user';
import { makeRequest } from '$lib/server/apis/api';
import { HttpRequest } from '$lib/server/utils/util';
import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';

export const load = (async ({ params, cookies }) => {
  const response = await makeRequest(
    {
      method: HttpRequest.GET,
      path: `/users/username/${params.name}`,
    },
    cookies,
  );

  if ('error' in response) error(response.status, { message: response.error });

  return { user: response as SimplifiedUser };
}) satisfies PageServerLoad;
