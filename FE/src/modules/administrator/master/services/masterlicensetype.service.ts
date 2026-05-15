import type { BaseResponse } from '@/modules/shared/models/base.response.ts'
import type { PageResponse } from '@/modules/shared/models/page.response.ts'
import type { LicenseTypePayload, LicenseTypeResponse } from '@/modules/administrator/master/models/licensetype.response.ts'
import type { LicensetypeRequest } from '@/modules/administrator/master/models/licensetype.request.ts'
import apiClient from '@/modules/shared/api/api.client.ts'

const BASE_PATH = '/licenses_type'
const SEARCH_PATH = '/licenses_type/search'

export interface LicenseTypePageParams {
  id?: string
  page: number
  size: number
  sortBy: string
  direction: 'asc' | 'desc'
}

export interface LicenseTypePageParamsWithFilter {
  filter: string
  page: number
  size: number
  sortBy: string
  direction: 'asc' | 'desc'
}

class MasterLicenseTypeService {
  async getAll(params: LicenseTypePageParams): Promise<BaseResponse<PageResponse<LicenseTypePayload>>> {
    const response = await apiClient.get<BaseResponse<PageResponse<LicenseTypePayload>>>(
      BASE_PATH,
      { params },
    )
    return response.data
  }

  async getAllByFilter(
    params: LicenseTypePageParamsWithFilter,
  ): Promise<BaseResponse<PageResponse<LicenseTypePayload>>> {
    const response = await apiClient.get<BaseResponse<PageResponse<LicenseTypePayload>>>(SEARCH_PATH, {
      params,
    })
    return response.data
  }

  async addLicenseType(request: LicensetypeRequest): Promise<LicenseTypeResponse> {
    const response = await apiClient.post<LicenseTypeResponse>(BASE_PATH, request)
    return response.data
  }

  async updateLicenseType(request: LicensetypeRequest): Promise<LicenseTypeResponse> {
    const response = await apiClient.patch<LicenseTypeResponse>(BASE_PATH, request)
    return response.data
  }

  async deleteLicenseType(id: string): Promise<void> {
    await apiClient.delete(`${BASE_PATH}/${id}`)
  }
}

export default new MasterLicenseTypeService()