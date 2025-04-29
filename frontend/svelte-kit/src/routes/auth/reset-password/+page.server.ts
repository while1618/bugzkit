import { env } from '$env/dynamic/private';
import * as m from '$lib/paraglide/messages.js';
import { apiErrors, makeRequest } from '$lib/server/apis/api';
import { HttpRequest } from '$lib/server/utils/util';
import { fail, redirect } from '@sveltejs/kit';
import jwt from 'jsonwebtoken';
import { setError, superValidate } from 'sveltekit-superforms';
import { zod } from 'sveltekit-superforms/adapters';
import type { Actions, PageServerLoad } from './$types';
import { resetPasswordSchema } from './schema';

export const load = (async ({ locals }) => {
  if (locals.userId) redirect(302, '/');

  const form = await superValidate(zod(resetPasswordSchema));
  return { form };
}) satisfies PageServerLoad;

export const actions = {
  resetPassword: async ({ request, cookies, url }) => {
    const form = await superValidate(request, zod(resetPasswordSchema));
    if (!form.valid) return fail(400, { form });

    const token = url.searchParams.get('token') ?? '';
    try {
      jwt.verify(token, env.JWT_SECRET);
    } catch (_) {
      return setError(form, m.auth_tokenInvalid());
    }

    const response = await makeRequest(
      {
        method: HttpRequest.POST,
        path: '/auth/password/reset',
        body: JSON.stringify({ ...form.data, token }),
      },
      cookies,
    );

    if ('error' in response) return apiErrors(response, form);

    redirect(302, '/auth/sign-in');
  },
} satisfies Actions;
