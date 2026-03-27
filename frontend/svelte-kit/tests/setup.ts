import { execSync } from 'child_process';
import { mkdirSync } from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const COMPOSE_FILE = path.resolve(__dirname, '../../../docker-compose-api.dev.yml');
const API_HEALTH_URL = 'http://localhost:8080/actuator/health';
const API_SIGNIN_URL = 'http://localhost:8080/auth/sign-in';

async function waitForApi(timeoutMs = 120_000): Promise<void> {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    try {
      const health = await fetch(API_HEALTH_URL);
      if (!health.ok) {
        await new Promise((r) => setTimeout(r, 2000));
        continue;
      }
      // Services are up, verify the data is seeded
      const probe = await fetch(API_SIGNIN_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ usernameOrEmail: 'user', password: 'wrong' }),
      });
      // 401 means the endpoint is reachable and the user record exists
      if (probe.status === 401) return;
    } catch {
      // not ready yet
    }
    await new Promise((r) => setTimeout(r, 2000));
  }
  throw new Error(`API did not become ready within ${timeoutMs / 1000}s`);
}

export default async function globalSetup() {
  mkdirSync(path.resolve(__dirname, '.auth'), { recursive: true });
  execSync(`docker compose -f "${COMPOSE_FILE}" down -v --remove-orphans`, { stdio: 'inherit' });
  execSync(`docker compose -f "${COMPOSE_FILE}" up --build -d`, {
    stdio: 'inherit',
    env: { ...process.env, RATE_LIMIT_ENABLED: 'false' },
  });
  console.log('Waiting for services to start...');
  await waitForApi();
  console.log('Services are up and running.');
}
