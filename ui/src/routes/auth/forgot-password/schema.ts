import * as m from '$lib/paraglide/messages.js';
import { EMAIL_REGEX } from '$lib/regex';
import { z } from 'zod';

export const forgotPasswordSchema = z.object({
  email: z.string().regex(EMAIL_REGEX, { error: m.auth_emailInvalid() }),
});
