export interface Device {
  deviceId: string;
  userAgent: string | null;
  createdAt: string;
  lastActiveAt: string;
  current: boolean;
}
