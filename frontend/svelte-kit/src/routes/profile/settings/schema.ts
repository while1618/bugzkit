import * as m from '$lib/paraglide/messages.js';
import { EMAIL_REGEX, PASSWORD_REGEX, USERNAME_REGEX } from '$lib/regex';
import { z, ZodIssueCode } from 'zod';

export const updateProfileSchema = z.object({
  username: z.string().regex(USERNAME_REGEX, { message: m.profile_usernameInvalid() }),
  email: z.string().regex(EMAIL_REGEX, { message: m.profile_emailInvalid() }),
});

export const changePasswordSchema = z
  .object({
    currentPassword: z.string().regex(PASSWORD_REGEX, { message: m.profile_passwordInvalid() }),
    newPassword: z.string().regex(PASSWORD_REGEX, { message: m.profile_passwordInvalid() }),
    confirmNewPassword: z.string().regex(PASSWORD_REGEX, { message: m.profile_passwordInvalid() }),
  })
  .superRefine(({ newPassword, confirmNewPassword }, ctx) => {
    if (newPassword !== confirmNewPassword) {
      ctx.addIssue({
        code: ZodIssueCode.custom,
        path: ['confirmNewPassword'],
        message: m.profile_passwordsDoNotMatch(),
      });
    }
  });

export const deleteSchema = z.object({});

export const revokeDeviceSchema = z.object({
  deviceId: z.string(),
});
