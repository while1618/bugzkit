import * as m from '$lib/paraglide/messages.js';
import { USERNAME_REGEX } from '$lib/regex';
import { z } from 'zod';

export const setUsernameSchema = z.object({
  username: z.string().regex(USERNAME_REGEX, { message: m.profile_usernameInvalid() }),
});
