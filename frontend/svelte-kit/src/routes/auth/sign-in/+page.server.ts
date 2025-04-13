import { apiErrors, makeRequest } from '$lib/server/apis/api';
import { HttpRequest } from '$lib/server/utils/util';
import { fail, redirect } from '@sveltejs/kit';
import { superValidate } from 'sveltekit-superforms';
import { zod } from 'sveltekit-superforms/adapters';
import type { Actions, PageServerLoad } from './$types';
import { signInSchema } from './schema';

export const load = (async ({ locals }) => {
  if (locals.userId) redirect(302, '/');

  return {
    form: await superValidate(zod(signInSchema)),
  };
}) satisfies PageServerLoad;

export const actions = {
  signIn: async ({ request, cookies }) => {
    const form = await superValidate(request, zod(signInSchema));
    if (!form.valid) return fail(400, { form });

    const response = await makeRequest(
      {
        method: HttpRequest.POST,
        path: '/auth/tokens',
        body: JSON.stringify(form.data),
      },
      cookies,
    );

    if ('error' in response) return apiErrors(response, form);

    redirect(302, '/');
  },
} satisfies Actions;
