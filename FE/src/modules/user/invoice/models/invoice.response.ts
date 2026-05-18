export interface InvoiceItem {
  id: string
  orderId: string
  invoiceNumber: string
  amount: number
  currency: string
  status: number
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
