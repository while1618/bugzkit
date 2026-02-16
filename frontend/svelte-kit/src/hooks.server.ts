import { env } from '$env/dynamic/private';
import { i18n } from '$lib/i18n';
import type { JwtPayload } from '$lib/models/auth/jwt-payload';
import { RoleName } from '$lib/models/user/role';
import { languageTag } from '$lib/paraglide/runtime';
import { makeRequest } from '$lib/server/apis/api';
import { HttpRequest, removeAuth } from '$lib/server/utils/util';
import { redirect, type Cookies, type Handle } from '@sveltejs/kit';
import { sequence } from '@sveltejs/kit/hooks';
import jwt from 'jsonwebtoken';

const tryToGetSignedInUser: Handle = async ({ event, resolve }) => {
  try {
    const accessToken = event.cookies.get('accessToken') ?? '';
    const { iss } = jwt.verify(accessToken, env.JWT_SECRET) as JwtPayload;
    event.locals.userId = iss;
  } catch (_) {
    await tryToRefreshToken(event.cookies, event.locals, event.request);
  }
  return await resolve(event);
};

async function tryToRefreshToken(
  cookies: Cookies,
  locals: App.Locals,
  request: Request,
): Promise<void> {
  try {
    const refreshToken = cookies.get('refreshToken') ?? '';
    jwt.verify(refreshToken, env.JWT_SECRET);
    const response = await makeRequest(
      {
        method: HttpRequest.POST,
        path: '/auth/tokens/refresh',
      },
      cookies,
      request,
    );

    if ('error' in response) {
      removeAuth(cookies, locals);
    } else {
      const accessToken = cookies.get('accessToken') ?? '';
      const { iss } = jwt.decode(accessToken) as JwtPayload;
      locals.userId = iss;
    }
  } catch (_) {
    removeAuth(cookies, locals);
  }
}

const protectedRoutes = ['/profile', '/admin', '/auth/sign-out', '/auth/sign-out-from-all-devices'];

const checkProtectedRoutes: Handle = async ({ event, resolve }) => {
  const languageInPathRegex = new RegExp(`^/${languageTag()}/`);
  const pathWithoutLanguage = event.url.pathname.replace(languageInPathRegex, '/');
  if (protectedRoutes.some((route) => pathWithoutLanguage.startsWith(route))) {
    const accessToken = event.cookies.get('accessToken');
    if (!accessToken) redirect(302, '/');

    if (pathWithoutLanguage.startsWith('/admin')) {
      const { roles } = jwt.decode(accessToken) as JwtPayload;
      if (!roles?.includes(RoleName.ADMIN)) redirect(302, '/');
    }
  }
  return await resolve(event);
};

export const handle = sequence(i18n.handle(), tryToGetSignedInUser, checkProtectedRoutes);
