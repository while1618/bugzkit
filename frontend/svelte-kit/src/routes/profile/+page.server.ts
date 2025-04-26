import { apiErrors, makeRequest } from '$lib/server/apis/api';
import { HttpRequest } from '$lib/server/utils/util';
import { fail, redirect, type Actions } from '@sveltejs/kit';
import { superValidate } from 'sveltekit-superforms';
import { zod } from 'sveltekit-superforms/adapters';
import type { PageServerLoad } from './$types';
import { setUsernameSchema } from './schema';

export const load = (async ({ parent }) => {
  const { profile } = await parent();
  const setUsernameForm = await superValidate(zod(setUsernameSchema));
  return { setUsernameForm, profile };
}) satisfies PageServerLoad;

export const actions = {
  setUsername: async ({ request, cookies }) => {
    const form = await superValidate(request, zod(setUsernameSchema));
    if (!form.valid) return fail(400, { form });

    const response = await makeRequest(
      {
        method: HttpRequest.PATCH,
        path: '/profile',
        body: JSON.stringify(form.data),
      },
      cookies,
    );

    if ('error' in response) return apiErrors(response, form);

    redirect(302, '/');
  },
} satisfies Actions;
