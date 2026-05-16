import type { LicenseResponse, LicenseDetailResponse } from '@/modules/user/license/models/license.response.ts'
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

  async getDetail(userId: string, licenseId: string): Promise<LicenseDetailResponse> {
    const response = await apiClient.get<LicenseDetailResponse>(`${BASE_PATH}/${licenseId}/user/${userId}`)
    return response.data
  }
}

export default new LicenseService()
