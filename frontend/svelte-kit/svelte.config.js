import adapter from '@sveltejs/adapter-node';
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';

// superForm(data.form) triggers state_referenced_locally warnings because it captures the initial
// prop value intentionally, as superforms manages its own reactive state after initialization.
// Suppressed inline with `// svelte-ignore state_referenced_locally` on each superForm call.
// See: https://github.com/ciscoheat/sveltekit-superforms/issues/667
/** @type {import('@sveltejs/kit').Config} */
const config = {
  // Consult https://kit.svelte.dev/docs/integrations#preprocessors
  // for more information about preprocessors
  preprocess: [vitePreprocess()],

  kit: {
    adapter: adapter(),
  },
};

export default config;
