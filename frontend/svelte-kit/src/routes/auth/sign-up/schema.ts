import * as m from '$lib/paraglide/messages.js';
import { EMAIL_REGEX, PASSWORD_REGEX, USERNAME_REGEX } from '$lib/regex';
import { z } from 'zod';

export const signUpSchema = z
  .object({
    username: z.string().regex(USERNAME_REGEX, { error: m.auth_usernameInvalid() }),
    email: z.string().regex(EMAIL_REGEX, { error: m.auth_emailInvalid() }),
    password: z.string().regex(PASSWORD_REGEX, { error: m.auth_passwordInvalid() }),
    confirmPassword: z.string().regex(PASSWORD_REGEX, { error: m.auth_passwordInvalid() }),
  })
  .superRefine(({ password, confirmPassword }, ctx) => {
    if (password !== confirmPassword) {
      ctx.addIssue({
        code: 'custom',
        path: ['confirmPassword'],
        error: m.auth_passwordsDoNotMatch(),
      });
    }
  });
