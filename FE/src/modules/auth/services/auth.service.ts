import axios, { type AxiosError } from 'axios'
import Cookies from 'js-cookie';
import type { LoginRequest } from '../models/login.request';
import type { LoginResponse } from '../models/login.response';
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
        console.log(payload.sessionToken);
      }

      return response.data;
    } catch (error) {
      const axiosError = error as AxiosError<{ code?: string; message?: string }>;
      if (axiosError.response?.status === 401 || axiosError.response?.data?.code === 'AUTH_ERROR') {
        throw new AuthException(axiosError.response?.data?.message || 'Authentication failed')
      }
      throw axiosError.response?.data || axiosError.message;
    }
  }

  logout(): void {
    Cookies.remove('SID');
    localStorage.removeItem('menus');
    localStorage.removeItem('userId')
  }

  // getCurrentUser(): LoginResponse | null {
  //   const userStr = localStorage.getItem('user');
  //   return userStr ? JSON.parse(userStr) : null;
  // }
}

export default new AuthService();
