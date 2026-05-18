import type { InvoiceDetailResponse, InvoiceResponse } from '@/modules/user/invoice/models/invoice.response.ts'
import apiClient from '@/modules/shared/api/api.client.ts'

const BASE_PATH = '/invoices'

export interface InvoicePageParams {
  page: number
  size: number
  sortBy: string
  direction: 'asc' | 'desc'
}

class InvoiceService {
  async getMyInvoices(params: InvoicePageParams): Promise<InvoiceResponse> {
    const response = await apiClient.get<InvoiceResponse>(`${BASE_PATH}/me`, { params })
    return response.data
  }

  async getInvoiceDetail(invoiceId: string): Promise<InvoiceDetailResponse> {
    const response = await apiClient.get<InvoiceDetailResponse>(`${BASE_PATH}/me/${invoiceId}`)
    return response.data
  }
}

export default new InvoiceService()
