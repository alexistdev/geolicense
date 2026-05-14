import type { LicenseResponse } from '@/modules/user/license/models/license.response.ts'
import apiClient from '@/modules/shared/api/api.client.ts'

const BASE_PATH = '/licenses'

export interface LicensePageParams {
  page: number
  size: number
  sortBy: string
  direction: 'asc' | 'desc'
}

class LicenseService {
  async getAll(userId: string, params: LicensePageParams): Promise<LicenseResponse> {
    const response = await apiClient.get<LicenseResponse>(`${BASE_PATH}/user/${userId}`, { params })
    return response.data
  }
}

export default new LicenseService()
