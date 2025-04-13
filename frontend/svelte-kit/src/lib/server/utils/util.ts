import type { JwtPayload } from '$lib/models/auth/jwt-payload';
import { RoleName } from '$lib/models/user/role';
import { type Cookies } from '@sveltejs/kit';
import jwt from 'jsonwebtoken';
import * as setCookieParser from 'set-cookie-parser';

export enum HttpRequest {
  GET = 'GET',
  POST = 'POST',
  PUT = 'PUT',
  PATCH = 'PATCH',
  DELETE = 'DELETE',
}

export function setCookieFromString(cookie: string, cookies: Cookies) {
  if (cookie === '') return;

  const parsed = setCookieParser.parseString(cookie);
  const { name, value, path, sameSite, secure, httpOnly, ...opts } = parsed;

  if (name === undefined || value === undefined || path === undefined) return;

  const normalizedSameSite = (() => {
    if (sameSite === undefined || typeof sameSite === 'boolean') {
      return sameSite;
    }
    const lower = sameSite.toLowerCase();
    if (lower === 'lax' || lower === 'strict' || lower === 'none') {
      return lower;
    }
    return undefined;
  })();

  cookies.set(name, value, {
    ...opts,
    path,
    sameSite: normalizedSameSite,
    secure: secure ?? false,
    httpOnly: httpOnly ?? false,
  });
}

export function removeAuth(cookies: Cookies, locals: App.Locals): void {
  cookies.delete('accessToken', { path: '/' });
  cookies.delete('refreshToken', { path: '/' });
  locals.userId = null;
}

export function isAdmin(accessToken: string | undefined): boolean {
  if (!accessToken) return false;
  const { roles } = jwt.decode(accessToken) as JwtPayload;
  return roles?.includes(RoleName.ADMIN) ?? false;
}
