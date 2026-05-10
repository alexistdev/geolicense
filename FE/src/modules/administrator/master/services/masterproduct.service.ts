import type { BaseResponse } from '@/modules/shared/models/base.response.ts'
import apiClient from '@/modules/shared/api/api.client.ts'
import type { PageResponse } from '@/modules/administrator/master/models/page.response.ts'
import type { ProductResponse } from '@/modules/administrator/master/models/product.response.ts'

const BASE_PATH = '/products'

export interface ProductPageParams {
  page: number
  size: number
  sortBy: string
  direction: 'asc' | 'desc'
}

class MasterProductService {
  async getAll(params: ProductPageParams): Promise<BaseResponse<PageResponse<ProductResponse>>> {
    const response = await apiClient.get<BaseResponse<PageResponse<ProductResponse>>>(BASE_PATH, {
      params,
    })
    return response.data
  }
}

export default new MasterProductService()
