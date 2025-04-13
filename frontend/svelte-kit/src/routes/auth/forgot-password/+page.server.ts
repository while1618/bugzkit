import * as m from '$lib/paraglide/messages.js';
import { apiErrors, makeRequest } from '$lib/server/apis/api';
import { HttpRequest } from '$lib/server/utils/util';
import { fail, redirect } from '@sveltejs/kit';
import { message, superValidate } from 'sveltekit-superforms';
import { zod } from 'sveltekit-superforms/adapters';
import type { Actions, PageServerLoad } from './$types';
import { forgotPasswordSchema } from './schema';

export const load = (async ({ locals }) => {
  if (locals.userId) redirect(302, '/');

  return {
    form: await superValidate(zod(forgotPasswordSchema)),
  };
}) satisfies PageServerLoad;

export const actions = {
  forgotPassword: async ({ request, cookies }) => {
    const form = await superValidate(request, zod(forgotPasswordSchema));
    if (!form.valid) return fail(400, { form });

    const response = await makeRequest(
      {
        method: HttpRequest.POST,
        path: '/auth/password/forgot',
        body: JSON.stringify(form.data),
      },
      cookies,
    );

    if ('error' in response) return apiErrors(response, form);

    return message(form, m.auth_forgotPasswordSuccess());
  },
} satisfies Actions;
