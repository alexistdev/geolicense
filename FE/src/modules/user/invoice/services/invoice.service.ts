import type { InvoiceResponse } from '@/modules/user/invoice/models/invoice.response.ts'
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
}

export default new InvoiceService()
