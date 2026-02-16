import type { Device } from '$lib/models/auth/device';
import * as m from '$lib/paraglide/messages.js';
import { apiErrors, makeRequest } from '$lib/server/apis/api';
import { HttpRequest, removeAuth } from '$lib/server/utils/util';
import { error, redirect } from '@sveltejs/kit';
import { fail, message, superValidate } from 'sveltekit-superforms';
import { zod } from 'sveltekit-superforms/adapters';
import type { Actions, PageServerLoad } from './$types';
import {
  changePasswordSchema,
  deleteSchema,
  revokeDeviceSchema,
  updateProfileSchema,
} from './schema';

export const load = (async ({ parent, cookies }) => {
  const changePasswordForm = await superValidate(zod(changePasswordSchema));
  const deleteForm = await superValidate(zod(deleteSchema));
  const revokeDeviceForm = await superValidate(zod(revokeDeviceSchema));
  const { profile } = await parent();
  const updateProfileInitialData = { username: profile?.username, email: profile?.email };
  const updateProfileForm = await superValidate(updateProfileInitialData, zod(updateProfileSchema));

  const devicesResponse = await makeRequest(
    { method: HttpRequest.GET, path: '/auth/tokens/devices' },
    cookies,
  );
  if ('error' in devicesResponse) error(devicesResponse.status, { message: devicesResponse.error });
  const devices = devicesResponse as unknown as Device[];

  return { updateProfileForm, changePasswordForm, deleteForm, revokeDeviceForm, devices, profile };
}) satisfies PageServerLoad;

export const actions = {
  updateProfile: async ({ request, cookies }) => {
    const form = await superValidate(request, zod(updateProfileSchema));
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

    return message(form, m.profile_updateSuccess());
  },
  changePassword: async ({ request, cookies, url }) => {
    const form = await superValidate(request, zod(changePasswordSchema));
    if (!form.valid) return fail(400, { form });

    const response = await makeRequest(
      {
        method: HttpRequest.PATCH,
        path: '/profile/password',
        body: JSON.stringify(form.data),
      },
      cookies,
    );

    if ('error' in response) return apiErrors(response, form);

    const signInResponse = await makeRequest(
      {
        method: HttpRequest.POST,
        path: '/auth/tokens',
        body: JSON.stringify({
          usernameOrEmail: url.searchParams.get('username'),
          password: form.data.newPassword,
        }),
      },
      cookies,
      request,
    );

    if ('error' in signInResponse) return apiErrors(signInResponse, form);

    return message(form, m.profile_changePasswordSuccess());
  },
  delete: async ({ request, cookies, locals }) => {
    const form = await superValidate(request, zod(deleteSchema));

    const response = await makeRequest(
      {
        method: HttpRequest.DELETE,
        path: '/profile',
      },
      cookies,
    );

    if ('error' in response) return apiErrors(response, form);

    removeAuth(cookies, locals);
    redirect(302, '/');
  },
  revokeDevice: async ({ request, cookies }) => {
    const form = await superValidate(request, zod(revokeDeviceSchema));
    if (!form.valid) return fail(400, { form });

    const response = await makeRequest(
      {
        method: HttpRequest.DELETE,
        path: `/auth/tokens/devices/${form.data.deviceId}`,
      },
      cookies,
    );

    if ('error' in response) return apiErrors(response, form);

    return message(form, m.profile_devicesRevokeSuccess());
  },
} satisfies Actions;
