import type { BaseResponse } from '@/modules/shared/models/base.response.ts'
import apiClient from '@/modules/shared/api/api.client.ts'
import type { PageResponse } from '@/modules/administrator/master/models/page.response.ts'
import type { ProductPayload, ProductResponse } from '@/modules/administrator/master/models/product.response.ts'
import type { ProductRequest } from '@/modules/administrator/master/models/product.request.ts'

const BASE_PATH = '/products'
const SEARCH_PATH = '/products/search'

export interface ProductPageParams {
  id?: string
  page: number
  size: number
  sortBy: string
  direction: 'asc' | 'desc'
}

export interface ProductPageParamsWithFilters {
  filter: string
  page: number
  size: number
  sortBy: string
  direction: 'asc' | 'desc'
}

class MasterProductService {
  async getAll(params: ProductPageParams): Promise<BaseResponse<PageResponse<ProductPayload>>> {
    const response = await apiClient.get<BaseResponse<PageResponse<ProductPayload>>>(BASE_PATH, {
      params,
    })
    return response.data
  }

  async getAllByFilter(
    params: ProductPageParamsWithFilters,
  ): Promise<BaseResponse<PageResponse<ProductPayload>>> {
    const response = await apiClient.get<BaseResponse<PageResponse<ProductPayload>>>(SEARCH_PATH, {
      params,
    })
    return response.data
  }

  async addProduct(request: ProductRequest): Promise<ProductResponse> {
    const response = await apiClient.post<ProductResponse>(BASE_PATH, request)
    return response.data
  }

  async updateProduct(request: ProductRequest): Promise<ProductResponse> {
    const response = await apiClient.patch<ProductResponse>(BASE_PATH, request)
    return response.data
  }

  async deleteProduct(id: string): Promise<void> {
    await apiClient.delete(`${BASE_PATH}/${id}`)
  }
}

export default new MasterProductService()
