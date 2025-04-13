import type { Pageable } from '$lib/models/shared/pageable';
import { type Role } from '$lib/models/user/role';
import type { AdminUser } from '$lib/models/user/user';
import * as m from '$lib/paraglide/messages.js';
import { apiErrors, makeRequest } from '$lib/server/apis/api';
import { HttpRequest } from '$lib/server/utils/util';
import { error, fail } from '@sveltejs/kit';
import { message, superValidate } from 'sveltekit-superforms';
import { zod } from 'sveltekit-superforms/adapters';
import type { Actions, PageServerLoad } from './$types';
import { actionSchema, changeRolesSchema, createSchema } from './schema';

export const load = (async ({ cookies, url }) => {
  let page = Number(url.searchParams.get('page')) || 1;
  let size = Number(url.searchParams.get('size')) || 10;

  if (page < 1) page = 1;
  if (size < 1) size = 10;

  const usersResponse = await makeRequest(
    {
      method: HttpRequest.GET,
      path: `/admin/users?page=${page}&size=${size}`,
    },
    cookies,
  );

  if ('error' in usersResponse) error(usersResponse.status, { message: usersResponse.error });

  const rolesResponse = await makeRequest(
    {
      method: HttpRequest.GET,
      path: `/roles`,
    },
    cookies,
  );

  if ('error' in rolesResponse) error(rolesResponse.status, { message: rolesResponse.error });

  const createForm = await superValidate(zod(createSchema));
  const activateForm = await superValidate(zod(actionSchema));
  const lockForm = await superValidate(zod(actionSchema));
  const deleteForm = await superValidate(zod(actionSchema));
  const changeRolesForm = await superValidate(zod(changeRolesSchema));

  return {
    users: usersResponse as Pageable<AdminUser>,
    roles: rolesResponse as Role[],
    pageable: { page, size },
    createForm,
    activateForm,
    lockForm,
    deleteForm,
    changeRolesForm,
  };
}) satisfies PageServerLoad;

export const actions = {
  create: async ({ request, cookies }) => {
    const form = await superValidate(request, zod(createSchema));
    if (!form.valid) return fail(400, { form });

    const response = await makeRequest(
      {
        method: HttpRequest.POST,
        path: `/admin/users`,
        body: JSON.stringify(form.data),
      },
      cookies,
    );

    if ('error' in response) return apiErrors(response, form);

    return message(form, m.admin_userCreatedSuccess());
  },
  activate: async ({ request, cookies }) => {
    const form = await superValidate(request, zod(actionSchema));

    const response = await makeRequest(
      {
        method: HttpRequest.PATCH,
        path: `/admin/users/${form.data.id}`,
        body: JSON.stringify({ active: true }),
      },
      cookies,
    );

    if ('error' in response) return apiErrors(response, form);

    return message(form, m.admin_activateSuccess());
  },
  deactivate: async ({ request, cookies }) => {
    const form = await superValidate(request, zod(actionSchema));

    const response = await makeRequest(
      {
        method: HttpRequest.PATCH,
        path: `/admin/users/${form.data.id}`,
        body: JSON.stringify({ active: false }),
      },
      cookies,
    );

    if ('error' in response) return apiErrors(response, form);

    return message(form, m.admin_deactivateSuccess());
  },
  unlock: async ({ request, cookies }) => {
    const form = await superValidate(request, zod(actionSchema));

    const response = await makeRequest(
      {
        method: HttpRequest.PATCH,
        path: `/admin/users/${form.data.id}`,
        body: JSON.stringify({ lock: false }),
      },
      cookies,
    );

    if ('error' in response) return apiErrors(response, form);

    return message(form, m.admin_unlockSuccess());
  },
  lock: async ({ request, cookies }) => {
    const form = await superValidate(request, zod(actionSchema));

    const response = await makeRequest(
      {
        method: HttpRequest.PATCH,
        path: `/admin/users/${form.data.id}`,
        body: JSON.stringify({ lock: true }),
      },
      cookies,
    );

    if ('error' in response) return apiErrors(response, form);

    return message(form, m.admin_lockSuccess());
  },
  delete: async ({ request, cookies }) => {
    const form = await superValidate(request, zod(actionSchema));

    const response = await makeRequest(
      {
        method: HttpRequest.DELETE,
        path: `/admin/users/${form.data.id}`,
      },
      cookies,
    );

    if ('error' in response) return apiErrors(response, form);

    return message(form, m.admin_deleteUserSuccess());
  },
  changeRoles: async ({ request, cookies }) => {
    const form = await superValidate(request, zod(changeRolesSchema));

    const response = await makeRequest(
      {
        method: HttpRequest.PATCH,
        path: `/admin/users/${form.data.id}`,
        body: JSON.stringify({ roleNames: form.data.roleNames }),
      },
      cookies,
    );

    if ('error' in response) return apiErrors(response, form);

    return message(form, m.admin_rolesSuccess());
  },
} satisfies Actions;
