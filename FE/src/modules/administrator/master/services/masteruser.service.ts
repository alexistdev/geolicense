import apiClient from '@/modules/shared/api/api.client'
import type { BaseResponse } from '@/modules/shared/models/base.response'
import type { UserResponse, PageResponse } from '../models/user.response'
import type { RegisterRequest } from '../models/register.request'
import type { RegisterResponse } from '../models/register.response'

const BASE_PATH = '/users'
const REGISTER_PATH = '/auth/register'

export interface UserPageParams {
  page: number
  size: number
  sortBy: string
  direction: 'asc' | 'desc'
}

class MasterUserService {
  async getAll(params: UserPageParams): Promise<BaseResponse<PageResponse<UserResponse>>> {
    const response = await apiClient.get<BaseResponse<PageResponse<UserResponse>>>(BASE_PATH, {
      params,
    })
    return response.data
  }

  async register(payload: RegisterRequest): Promise<RegisterResponse> {
    const response = await apiClient.post<RegisterResponse>(REGISTER_PATH, payload)
    return response.data
  }
}

export default new MasterUserService()
