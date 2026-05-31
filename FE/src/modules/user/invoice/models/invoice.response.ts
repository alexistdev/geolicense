export type InvoiceStatus = 'UNPAID' | 'AWAITING_VERIFICATION' | 'PAID' | 'CANCELLED'

export interface InvoiceItem {
  id: string
  orderNumber: string
  invoiceNumber: string
  amount: number
  uniqueCode: number
  totalAmount: number
  currency: string
  status: InvoiceStatus
  issuedAt: string
}

export interface InvoicePagePayload {
  content: InvoiceItem[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface InvoiceResponse {
  messages: string[]
  payload: InvoicePagePayload
  status: boolean
}

export interface OrderItemDetail {
  quantity: number
  unitPrice: number
  totalPrice: number
  planName: string
  billingCycle: string
  durationDays: number
  maxSeats: number
  productName: string
  productVersion: string
  licenseTypeName: string
  isTrial: boolean
}

export interface InvoiceDetail {
  id: string
  invoiceNumber: string
  orderNumber: string
  amount: number
  discount: number
  tax: number
  uniqueCode: number
  totalAmount: number
  currency: string
  status: InvoiceStatus
  issuedAt: string
  items: OrderItemDetail[]
}

export interface InvoiceDetailResponse {
  messages: string[]
  payload: InvoiceDetail
  status: boolean
}
