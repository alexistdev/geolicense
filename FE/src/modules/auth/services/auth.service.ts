import axios, { type AxiosError } from 'axios'
import Cookies from 'js-cookie';
import type { LoginRequest } from '../models/login.request';
import type { LoginResponse, Menu } from '../models/login.response';
import type { BaseResponse } from '../../shared/models/base.response';
import { AuthException } from '@/modules/auth/exception/auth.exception.ts'

const API_URL = 'http://localhost:8082/api/v1/auth/';

axios.defaults.withCredentials = true;

class AuthService {
  async login(credentials: LoginRequest): Promise<BaseResponse<LoginResponse>> {
    try {
      const response = await axios.post<BaseResponse<LoginResponse>>(API_URL + 'login', credentials);
      const { payload } = response.data;

      if (payload && payload.sessionToken) {
        Cookies.set('SID', payload.sessionToken);
        localStorage.setItem('menus', JSON.stringify(payload.menus));
        localStorage.setItem('userId', JSON.stringify(payload.id));
        localStorage.setItem('fullName', payload.fullName ?? '');
        localStorage.setItem('role', payload.role);
      }

      return response.data;
    } catch (error) {
      const axiosError = error as AxiosError<{ code?: string; message?: string; messages?: string[] }>;
      if (axiosError.response?.status === 401 || axiosError.response?.data?.code === 'AUTH_ERROR') {
        const data = axiosError.response?.data;
        const errorMessage = ((data?.messages && data.messages.length > 0)
          ? data.messages[0]
          : data?.message) || 'Authentication failed';
        throw new AuthException(errorMessage);
      }
      throw axiosError.response?.data || axiosError.message;
    }
  }

  logout(): void {
    Cookies.remove('SID');
    localStorage.removeItem('menus');
    localStorage.removeItem('userId');
    localStorage.removeItem('fullName');
    localStorage.removeItem('role');
  }

  getMenus(): Menu[] | null {
    const menus = localStorage.getItem('menus');
    return menus ? JSON.parse(menus) : null
  }

  getFullName(): string {
    return localStorage.getItem('fullName') ?? ''
  }

  getRole(): string {
    return localStorage.getItem('role') ?? ''
  }
}

export default new AuthService();
