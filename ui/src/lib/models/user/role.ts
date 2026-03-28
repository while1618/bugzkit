import type { components } from '../api';

export type Role = components['schemas']['RoleDTO'];

// Kept as enum for runtime usage (RoleName.ADMIN, RoleName.USER)
export enum RoleName {
  USER = 'USER',
  ADMIN = 'ADMIN',
}
