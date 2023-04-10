import { JWT_SECRET } from '$env/static/private';
import { HttpRequest, makeRequest } from '$lib/apis/api';
import type { JwtPayload } from '$lib/models/jwt-payload';
import type { RefreshTokenDTO } from '$lib/models/refresh-token';
import { RoleName } from '$lib/models/role';
import { removeBearerPrefix, setAccessTokenCookie, setRefreshTokenCookie } from '$lib/utils/util';
import { redirect, type Cookies, type Handle } from '@sveltejs/kit';
import jwt from 'jsonwebtoken';

const protectedRoutes = [
  '/auth/sign-out',
  '/auth/sign-out-from-all-devices',
  '/user/settings',
  '/admin/dashboard',
];

export const handle = (async ({ event, resolve }) => {
  await tryToGetSignedInUser(event.cookies, event.locals);
  checkProtectedRoutes(event.url, event.cookies);

  return resolve(event);
}) satisfies Handle;

async function tryToGetSignedInUser(cookies: Cookies, locals: App.Locals): Promise<void> {
  try {
    const accessToken = cookies.get('accessToken') ?? '';
    const payload = jwt.verify(removeBearerPrefix(accessToken), JWT_SECRET) as JwtPayload;
    locals.userId = payload.iss;
  } catch (error) {
    await tryToRefreshToken(cookies, locals);
  }
}

async function tryToRefreshToken(cookies: Cookies, locals: App.Locals): Promise<void> {
  const response = await makeRequest({
    method: HttpRequest.POST,
    path: '/auth/refresh-token',
    body: JSON.stringify({ refreshToken: cookies.get('refreshToken') }),
  });

  if ('error' in response) {
    cookies.delete('accessToken', { path: '/' });
    cookies.delete('refreshToken', { path: '/' });
    locals.userId = null;
  } else {
    const { accessToken, refreshToken } = response as RefreshTokenDTO;
    setAccessTokenCookie(cookies, accessToken);
    setRefreshTokenCookie(cookies, refreshToken);
    const { iss } = jwt.decode(removeBearerPrefix(accessToken)) as JwtPayload;
    locals.userId = iss;
  }
}

function checkProtectedRoutes(url: URL, cookies: Cookies): void {
  if (protectedRoutes.includes(url.pathname)) {
    const accessToken = cookies.get('accessToken');
    if (!accessToken) throw redirect(302, '/');

    if (url.pathname.startsWith('/admin')) {
      const { roles } = jwt.decode(removeBearerPrefix(accessToken)) as JwtPayload;
      if (!roles?.includes(RoleName.ADMIN)) throw redirect(302, '/');
    }
  }
}
