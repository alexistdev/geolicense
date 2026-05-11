export interface ProductPayload {
  id: string
  name: string
  version: string
  description: string
  sku: string
  active: boolean
}

export interface ProductResponse {
  messages: string[]
  payload: ProductPayload
  status: boolean
}
