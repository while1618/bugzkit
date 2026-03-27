import { execSync } from 'child_process';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const COMPOSE_FILE = path.resolve(__dirname, '../../../docker-compose-api.dev.yml');

export default async function globalTeardown() {
  execSync(`docker compose -f "${COMPOSE_FILE}" down -v --remove-orphans`, { stdio: 'inherit' });
}
