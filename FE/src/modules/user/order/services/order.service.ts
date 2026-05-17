import type { BaseResponse } from '@/modules/shared/models/base.response.ts'
import type { CreateOrderRequest, CreateOrderResponse } from '@/modules/user/order/models/order.model.ts'
import apiClient from '@/modules/shared/api/api.client.ts'

class OrderService {
  async createOrder(request: CreateOrderRequest): Promise<BaseResponse<CreateOrderResponse>> {
    const response = await apiClient.post<BaseResponse<CreateOrderResponse>>('/orders', request)
    return response.data
  }
}

export default new OrderService()
