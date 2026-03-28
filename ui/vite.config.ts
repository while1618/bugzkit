import tailwindcss from '@tailwindcss/vite';
import { paraglideVitePlugin } from '@inlang/paraglide-js';
import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vitest/config';

export default defineConfig({
  plugins: [
    tailwindcss(),
    paraglideVitePlugin({ project: './project.inlang', outdir: './src/lib/paraglide' }),
    sveltekit(),
  ],
  server: {
    port: process.env.PORT ? parseInt(process.env.PORT) : 5173,
    strictPort: true,
  },
  test: {
    include: ['src/**/*.{test,spec}.{js,ts}'],
  },
});
