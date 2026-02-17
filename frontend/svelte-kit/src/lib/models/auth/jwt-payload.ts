import type { RoleName } from '../user/role';

export interface JwtPayload {
  iss: string;
  iat: number;
  exp: number;
  purpose: JwtPurpose;
  roles?: RoleName[];
  deviceId: string;
}

export enum JwtPurpose {
  ACCESS_TOKEN = 'ACCESS_TOKEN',
  REFRESH_TOKEN = 'REFRESH_TOKEN',
}
