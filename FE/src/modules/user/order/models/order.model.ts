export interface CreateOrderRequest {
  licensePlanId: string
  quantity: number
}

export interface CreateOrderResponse {
  orderId: string
  orderNumber: string
  totalAmount: number
  currency: string
  status: number
}
