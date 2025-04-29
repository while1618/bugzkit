import * as m from '$lib/paraglide/messages.js';
import { PASSWORD_REGEX } from '$lib/regex';
import { z, ZodIssueCode } from 'zod';

export const resetPasswordSchema = z
  .object({
    password: z.string().regex(PASSWORD_REGEX, { message: m.auth_passwordInvalid() }),
    confirmPassword: z.string().regex(PASSWORD_REGEX, { message: m.auth_passwordInvalid() }),
  })
  .superRefine(({ password, confirmPassword }, ctx) => {
    if (password !== confirmPassword) {
      ctx.addIssue({
        code: ZodIssueCode.custom,
        path: ['confirmPassword'],
        message: m.auth_passwordsDoNotMatch(),
      });
    }
  });
