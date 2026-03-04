import type { components } from '../api';
import type { Role } from './role';

type UserDTO = Required<components['schemas']['UserDTO']>;

export type AdminUser = Omit<UserDTO, 'active' | 'lock' | 'roles'> & {
  active: boolean;
  lock: boolean;
  roles: Role[];
};

export type Profile = Pick<UserDTO, 'id' | 'username' | 'email' | 'createdAt'>;

export type SimplifiedUser = Pick<UserDTO, 'id' | 'username' | 'createdAt'>;
