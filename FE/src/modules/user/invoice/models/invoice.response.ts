export interface InvoiceItem {
  id: string
  orderNumber: string
  invoiceNumber: string
  amount: number
  currency: string
  status: number
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
