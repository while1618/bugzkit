import { defineConfig, globalIgnores } from 'eslint/config';
import nextVitals from 'eslint-config-next/core-web-vitals';
import nextTs from 'eslint-config-next/typescript';
import prettier from 'eslint-config-prettier/flat';

const eslintConfig = defineConfig([
  ...nextVitals,
  ...nextTs,
  prettier,
  {
    settings: {
      react: { version: '19' },
    },
  },
  globalIgnores(['node_modules/', '.next/', 'next-env.d.ts', 'out/']),
]);

export default eslintConfig;
