import axios from 'axios';
import Cookies from 'js-cookie';
import type { LoginRequest } from '../models/login.request';
import type { LoginResponse } from '../models/login.response';
import type { BaseResponse } from '../../shared/models/base.response';

const API_URL = 'http://localhost:8082/api/v1/auth/';

// Enable sending cookies with cross-origin requests
axios.defaults.withCredentials = true;

class AuthService {
  async login(credentials: LoginRequest): Promise<BaseResponse<LoginResponse>> {
    try {
      const response = await axios.post<BaseResponse<LoginResponse>>(API_URL + 'login', credentials);
      const { payload } = response.data;
      
      if (payload && payload.sessionToken) {
        Cookies.set('SID', payload.sessionToken);
        localStorage.setItem('user', JSON.stringify(payload));
      }
      
      return response.data;
    } catch (error: any) {
      throw error.response?.data || error.message;
    }
  }

  logout(): void {
    Cookies.remove('SID');
    localStorage.removeItem('user');
  }

  getCurrentUser(): LoginResponse | null {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  }
}

export default new AuthService();
