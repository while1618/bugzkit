import { paraglide } from '@inlang/paraglide-sveltekit/vite';
import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vitest/config';

export default defineConfig({
  plugins: [paraglide({ project: './project.inlang', outdir: './src/lib/paraglide' }), sveltekit()],
  server: {
    port: process.env.PORT ? parseInt(process.env.PORT) : 5173,
    strictPort: true,
  },
  test: {
    include: ['src/**/*.{test,spec}.{js,ts}'],
  },
});
