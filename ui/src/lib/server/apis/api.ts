import { PUBLIC_API_URL } from '$env/static/public';
import { ErrorCode, type ErrorMessage } from '$lib/models/shared/error-message';
import * as m from '$lib/paraglide/messages.js';
import { setCookiesFromString, type HttpRequest } from '$lib/server/utils/util';
import { fail, type Cookies } from '@sveltejs/kit';
import { setError, type SuperValidated } from 'sveltekit-superforms';

interface RequestParams {
  method: HttpRequest;
  path: string;
  body?: string;
}

export async function makeRequest(
  params: RequestParams,
  cookies: Cookies,
  request?: Request,
): Promise<object | ErrorMessage> {
  const opts: RequestInit = {};
  const headers = new Headers();

  const accessToken = cookies.get('accessToken');
  const refreshToken = cookies.get('refreshToken');
  if (accessToken) headers.append('Cookie', `accessToken=${accessToken}`);
  if (refreshToken) headers.append('Cookie', `refreshToken=${refreshToken}`);

  const userAgent = request?.headers.get('User-Agent');
  if (userAgent) headers.set('User-Agent', userAgent);

  if (params.body) {
    headers.append('Content-Type', 'application/json');
    opts.body = params.body;
  }

  opts.method = params.method;
  opts.headers = headers;

  const response = await fetch(`${PUBLIC_API_URL}${params.path}`, opts);

  const setCookies = response.headers.getSetCookie();
  setCookiesFromString(setCookies, cookies);

  if (!response.ok) return (await response.json()) as ErrorMessage;

  const text = await response.text();
  return text ? JSON.parse(text) : {};
}

export function apiErrors(
  errorMessage: ErrorMessage,
  form: SuperValidated<Record<string, unknown>>,
) {
  for (const code of errorMessage.codes) {
    const key = ErrorCode[code as keyof typeof ErrorCode];
    const message = key ? m[key]() : m.API_ERROR_UNKNOWN();
    setError(form, message);
  }
  return fail(errorMessage.status, { form });
}
