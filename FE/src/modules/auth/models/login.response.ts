export interface LoginResponse {
  id: string;
  sessionToken: string;
  role: 'USER' | 'ADMIN';
}

