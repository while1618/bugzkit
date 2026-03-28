import { deLocalizeUrl } from '$lib/paraglide/runtime.js';

export function reroute({ url }: { url: URL }): string {
  return deLocalizeUrl(url.href).pathname;
}
