import type { BaseResponse } from '@/modules/shared/models/base.response.ts'
import type { PageResponse } from '@/modules/shared/models/page.response.ts'
import type { ProductItem } from '@/modules/user/marketplace/models/marketplace.response.ts'
import apiClient from '@/modules/shared/api/api.client.ts'

const BASE_PATH = '/products'

export interface CatalogPageParams {
  id?: string
  page: number
  size: number
  sortBy: string
  direction: 'asc' | 'desc'
}

class MarketplaceService {
  async getAll(
    params: CatalogPageParams,
  ): Promise<BaseResponse<PageResponse<ProductItem>>> {
    const response = await apiClient.get<BaseResponse<PageResponse<ProductItem>>>(
      BASE_PATH,
      { params },
    )
    return response.data
  }
}

export default new MarketplaceService()
