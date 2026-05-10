import apiClient from '@/modules/shared/api/api.client'
import type { BaseResponse } from '@/modules/shared/models/base.response'
import type { UserResponse, PageResponse } from '../models/user.response'

const BASE_PATH = '/users'

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
}

export default new MasterUserService()
